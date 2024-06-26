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

package org.openlmis.stockmanagement.dto;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockEventDto {

  private UUID resourceId;

  private UUID facilityId;

  private UUID programId;

  private String signature;

  private String documentNumber;

  private UUID userId;

  private boolean isActive;

  private List<StockEventLineItemDto> lineItems;

  private StockEventProcessContext context;

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  /**
   * Convert dto to jpa model.
   *
   * @return the converted jpa model object.
   */
  public StockEvent toEvent() {
    List<StockEventLineItem> domainLines = this.lineItems
        .stream()
        .map(StockEventLineItemDto::toEventLineItem)
        .collect(Collectors.toList());

    StockEvent event = new StockEvent(facilityId, programId, context.getCurrentUserId(),
        now(),//processed date generated by server side
        isActive, signature, documentNumber, domainLines);

    domainLines.forEach(lineItem -> {
      lineItem.setStockAdjustments(lineItem.stockAdjustments());
      lineItem.setStockEvent(event);
    });

    return event;
  }

  /**
   * Checks if this event contains sources.
   */
  public boolean hasSource() {
    return hasLineItems() && getLineItems()
        .stream()
        .anyMatch(StockEventLineItemDto::hasSourceId);
  }

  /**
   * Checks if this event contains destinations.
   */
  public boolean hasDestination() {
    return hasLineItems() && getLineItems()
        .stream()
        .anyMatch(StockEventLineItemDto::hasDestinationId);
  }

  /**
   * Check if this event is about physical inventory.
   *
   * @return boolean value that represent if this event is physical inventory.
   */
  public boolean isPhysicalInventory() {
    boolean noReason = hasLineItems()
        && getLineItems().stream().noneMatch(StockEventLineItemDto::hasReasonId);
    return noReason && !hasDestination() && !hasSource();
  }

  public boolean hasLineItems() {
    return !isEmpty(getLineItems());
  }

  /**
   * Retrieves all reason IDs from event line items and related with them stock adjustments.
   */
  @JsonIgnore
  public Set<UUID> getReasonIds() {
    Set<UUID> reasonIds = new HashSet<>();

    for (StockEventLineItemDto lineItem : lineItems) {
      if (lineItem.hasReasonId()) {
        reasonIds.add(lineItem.getReasonId());
      }

      Set<UUID> adjustmentReasons = Optional
          .ofNullable(lineItem.getStockAdjustments())
          .orElse(emptyList())
          .stream()
          .map(StockEventAdjustmentDto::getReasonId)
          .collect(Collectors.toSet());

      reasonIds.addAll(adjustmentReasons);
    }

    return reasonIds;
  }

  /**
   * Retrieves all node IDS (both source and destination) from event line items.
   */
  @JsonIgnore
  public Set<UUID> getNodeIds() {
    Set<UUID> nodeIds = Sets.newHashSet();

    for (StockEventLineItemDto lineItem : lineItems) {
      if (lineItem.hasSourceId()) {
        nodeIds.add(lineItem.getSourceId());
      }

      if (lineItem.hasDestinationId()) {
        nodeIds.add(lineItem.getDestinationId());
      }
    }

    return nodeIds;
  }

  /**
   * Checks if a stock event is a kit unpacking event or not. Returns true if this is a kit
   * unpacking event.
   */
  @JsonIgnore
  public boolean isKitUnpacking() {
    return hasLineItems()
        && lineItems
        .stream()
        .anyMatch(l -> context.getUnpackReasonId().equals(l.getReasonId()));
  }

  /**
   * Sorts the EventDto line items by occurred date.
   */
  public void sortLineItemsByOccurreddate() {
    if (lineItems != null) {
      lineItems.sort(Comparator.comparing(StockEventLineItemDto::getOccurredDate).reversed());
    }
  }
}