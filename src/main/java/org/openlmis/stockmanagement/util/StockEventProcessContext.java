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

package org.openlmis.stockmanagement.util;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.sourcedestination.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;

/**
 * Before processing a stock event, one instance of this class will be created to hold all things
 * needed from ref-data service. By doing this, all network traffic is concentrated in one place,
 * not scattered around in different places. All resources use lazy loading so they are retrieved
 * only when there is a need.
 */
@Setter
public class StockEventProcessContext {

  private LazyResource<UUID> currentUserId;
  private LazyResource<ProgramDto> program;
  private LazyResource<FacilityDto> facility;

  private LazyList<OrderableDto> allApprovedProducts;
  private LazyList<ValidSourceAssignment> sources;
  private LazyList<ValidDestinationAssignment> destinations;

  private LazyGrouping<UUID, LotDto> lots;
  private LazyGrouping<UUID, StockCardLineItemReason> cardReasons;
  private LazyGrouping<UUID, StockCardLineItemReason> eventReasons;
  private LazyGrouping<UUID, Node> nodes;
  private LazyGrouping<OrderableLotIdentity, StockCard> cards;

  @Getter
  private UUID unpackReasonId;

  public UUID getCurrentUserId() {
    return currentUserId.get();
  }

  public ProgramDto getProgram() {
    return program.get();
  }

  public FacilityDto getFacility() {
    return facility.get();
  }

  public UUID getFacilityTypeId() {
    FacilityDto facilityDto = getFacility();
    return null == facilityDto ? null : facilityDto.getType().getId();
  }

  public Collection<OrderableDto> getAllApprovedProducts() {
    return allApprovedProducts.get();
  }

  public List<ValidSourceAssignment> getSources() {
    return sources.get();
  }

  public List<ValidDestinationAssignment> getDestinations() {
    return destinations.get();
  }

  public LotDto findLot(UUID lotId) {
    return lots.get().get(lotId);
  }

  public StockCardLineItemReason findEventReason(UUID reasonId) {
    return eventReasons.get().get(reasonId);
  }

  public StockCardLineItemReason findCardReason(UUID reasonId) {
    return cardReasons.get().get(reasonId);
  }

  public Node findNode(UUID nodeId) {
    return nodes.get().get(nodeId);
  }

  public StockCard findCard(OrderableLotIdentity identity) {
    return cards.get().get(identity);
  }

  public StockCard removeCard(OrderableLotIdentity identity) {
    return cards.get().remove(identity);
  }

  public void refreshCards() {
    cards.refresh();
  }
}
