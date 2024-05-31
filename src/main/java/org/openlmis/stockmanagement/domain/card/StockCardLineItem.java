/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.stockmanagement.domain.card;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static javax.persistence.CascadeType.ALL;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.ExtraDataConverter;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Getter
@Setter
@Builder
@ToString(exclude = "stockCard")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({
    "stockCard", "originEvent",
    "source", "destination",
    "processedDate",
    "userId"})
@Table(name = "stock_card_line_items", schema = "stockmanagement")
public class StockCardLineItem extends BaseEntity {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardLineItem.class);

  @ManyToOne
  @JoinColumn(nullable = false)
  private StockCard stockCard;

  @ManyToOne
  @JoinColumn(nullable = false)
  private StockEvent originEvent;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  private Map<String, String> extraData;

  @ManyToOne
  @JoinColumn
  private StockCardLineItemReason reason;

  private String sourceFreeText;
  private String destinationFreeText;
  private String documentNumber;
  private String reasonFreeText;
  private String signature;

  @ManyToOne
  @JoinColumn
  private Node source;

  @ManyToOne
  @JoinColumn
  private Node destination;

  @Column(nullable = false)
  @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")
  private LocalDate occurredDate;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime processedDate;

  @Column(nullable = false)
  private UUID userId;

  @Transient
  private Integer stockOnHand;

  @OneToMany(
      cascade = ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @JoinColumn(name = "stockCardLineItemId")
  private List<PhysicalInventoryLineItemAdjustment> stockAdjustments;

  /**
   * Create line item from eventDto.
   *
   * @param eventDto     stock eventDto.
   * @param stockCard    the card that this line item belongs to.
   * @param savedEventId saved event id.
   * @param processedDate processed date
   * @return created line item.
   */
  public static StockCardLineItem createLineItemFrom(
      StockEventDto eventDto, StockEventLineItemDto eventLineItemDto,
      StockCard stockCard, UUID savedEventId, ZonedDateTime processedDate) {
    StockCardLineItemBuilder builder = StockCardLineItem.builder();

    if (null != savedEventId) {
      StockEvent event = new StockEvent();
      event.setId(savedEventId);
      builder = builder.originEvent(event);
    }

    if (null != eventLineItemDto.getReasonId()) {
      builder = builder.reason(eventDto.getContext().findEventReason(
          eventLineItemDto.getReasonId()
      ));
    }

    if (null != eventLineItemDto.getSourceId()) {
      Node source = new Node();
      source.setId(eventLineItemDto.getSourceId());
      builder = builder.source(source);
    }

    if (null != eventLineItemDto.getDestinationId()) {
      Node destination = new Node();
      destination.setId(eventLineItemDto.getDestinationId());
      builder = builder.destination(destination);
    }

    StockCardLineItem cardLineItem = builder
        .stockCard(stockCard)

        .quantity(eventLineItemDto.getQuantity())
        .stockAdjustments(eventLineItemDto.stockAdjustments())

        .occurredDate(eventLineItemDto.getOccurredDate())
        .processedDate(processedDate)

        .reasonFreeText(eventLineItemDto.getReasonFreeText())
        .sourceFreeText(eventLineItemDto.getSourceFreeText())
        .destinationFreeText(eventLineItemDto.getDestinationFreeText())

        .documentNumber(eventDto.getDocumentNumber())
        .signature(eventDto.getSignature())
        .userId(eventDto.getContext().getCurrentUserId())

        .extraData(eventLineItemDto.getExtraData())
        .build();

    stockCard.getLineItems().add(cardLineItem);

    return cardLineItem;
  }

  /**
   * Returns quantity value with correct sign depending on reason type.
   *
   * @return quantity value, is negative for Debit reason
   */
  public Integer getQuantityWithSign() {
    if (null == this.getQuantity()) {
      return 0;
    }
    return this.isPositive()
        ? this.getQuantity()
        : this.getQuantity() * -1;
  }

  /**
   * Checks if assigned reason has tag assigned.
   *
   * @param tag string with tag value
   * @return true if there is reason assigned and has given tag
   */
  public boolean containsTag(String tag) {
    return null != this.getReason() && this.getReason().getTags().contains(tag);
  }

  /**
   * Checks if line item is physical inventory.
   *
   * @return true if is physical inventory
   */
  public boolean isPhysicalInventory() {
    return source == null && destination == null && reason == null;
  }

  /**
   * Checks if line item will add quantity to SoH.
   *
   * @return true if is physical inventory
   */
  public boolean isPositive() {
    boolean hasSource = source != null;
    boolean isCredit = reason != null && reason.getReasonType() == ReasonType.CREDIT;
    return hasSource || isCredit;
  }
}
