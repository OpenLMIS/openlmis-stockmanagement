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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.hibernate.annotations.LazyCollectionOption.FALSE;

import org.hibernate.annotations.LazyCollection;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "stock_cards", schema = "stockmanagement",
    indexes = @Index(columnList = "facilityId,programId,orderableId"))
//the above line creates an index, it'll make select statements faster
//especially for getStockCardIdBy method of StockCardRepository
public class StockCard extends BaseEntity {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCard.class);

  @ManyToOne()
  @JoinColumn(nullable = false)
  private StockEvent originEvent;

  @Column(nullable = false)
  private UUID facilityId;
  @Column(nullable = false)
  private UUID programId;
  @Column(nullable = false)
  private UUID orderableId;

  @LazyCollection(FALSE)
  @OneToMany(cascade = ALL, mappedBy = "stockCard")
  private List<StockCardLineItem> lineItems;

  @Transient
  private Integer stockOnHand = null;

  /**
   * Create stock card from stock event dto.
   *
   * @param stockEventDto the origin event dto.
   * @param savedEventId  the saved event id.
   * @return the created stock card.
   * @throws InstantiationException InstantiationException.
   * @throws IllegalAccessException IllegalAccessException.
   */
  public static StockCard createStockCardFrom(StockEventDto stockEventDto,
                                              StockEventLineItem eventLineItem, UUID savedEventId)
      throws InstantiationException, IllegalAccessException {
    return new StockCard(fromId(savedEventId, StockEvent.class),
        stockEventDto.getFacilityId(), stockEventDto.getProgramId(),
        eventLineItem.getOrderableId(), new ArrayList<>(), 0);
  }

  /**
   * Create a new instance of stock card with given id.
   *
   * @param foundCardId the stock card id.
   * @return the created instance.
   */
  public static StockCard newInstanceById(UUID foundCardId) {
    StockCard stockCard = new StockCard();
    stockCard.setId(foundCardId);
    stockCard.setLineItems(new ArrayList<>());
    return stockCard;
  }

  /**
   * Calculate stock on hand for each line item and the card itself.
   */
  public void calculateStockOnHand() {
    if (isEmpty(lineItems)) {
      return;
    }

    reorderLineItemsByDates();
    int previousSoh = 0;
    for (StockCardLineItem lineItem : getLineItems()) {
      lineItem.calculateStockOnHand(previousSoh);
      previousSoh = lineItem.getStockOnHand();
    }
    setStockOnHand(previousSoh);
    LOGGER.debug("Calculated stock on hand: " + previousSoh);
  }

  private void reorderLineItemsByDates() {
    Comparator<StockCardLineItem> byOccurred =
        comparing(StockCardLineItem::getOccurredDate);
    Comparator<StockCardLineItem> byProcessed =
        comparing(StockCardLineItem::getProcessedDate);

    setLineItems(lineItems.stream()
        .sorted(byOccurred.thenComparing(byProcessed))
        .collect(toList()));
  }
}
