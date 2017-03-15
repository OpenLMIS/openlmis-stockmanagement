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

package org.openlmis.stockmanagement.service;

import static java.util.stream.Collectors.toList;
import static org.openlmis.stockmanagement.domain.BaseEntity.fromId;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_DUPLICATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventory;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PhysicalInventoryService {

  @Autowired
  private StockEventProcessor stockEventProcessor;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  /**
   * Try to create physical inventory.
   *
   * @param dto physical inventory DTO
   * @return created physical inventory JPA model ID
   * @throws IllegalAccessException IllegalAccessException
   * @throws InstantiationException InstantiationException
   */
  public UUID createPhysicalInventory(PhysicalInventoryDto dto)
      throws IllegalAccessException, InstantiationException {
    validate(dto);
    return submitPhysicalInventory(dto);
  }

  /**
   * Find draft by program and facility.
   *
   * @param programId  programId.
   * @param facilityId facilityId.
   * @return found draft, or if not found, returns empty draft.
   */
  public PhysicalInventoryDto findDraft(UUID programId, UUID facilityId) {
    PhysicalInventory foundInventory = physicalInventoriesRepository
        .findByProgramIdAndFacilityIdAndIsDraft(programId, facilityId, true);
    if (foundInventory == null) {
      return createEmptyInventory(programId, facilityId);
    }

    return null;
  }

  private UUID submitPhysicalInventory(PhysicalInventoryDto physicalInventoryDto)
      throws InstantiationException, IllegalAccessException {
    PhysicalInventory inventory = physicalInventoryDto.toPhysicalInventory();
    for (StockEventDto eventDto : physicalInventoryDto.toEventDtos()) {
      UUID savedEventId = stockEventProcessor.process(eventDto);
      inventory.getStockEvents().add(fromId(savedEventId, StockEvent.class));
    }
    return physicalInventoriesRepository.save(inventory).getId();
  }

  private void validate(PhysicalInventoryDto dto) {
    List<PhysicalInventoryLineItemDto> lineItems = dto.getLineItems();
    if (lineItems == null || lineItems.isEmpty()) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING));
    }
    boolean orderableMissing = lineItems.stream()
        .anyMatch(lineItem -> lineItem.getOrderable() == null);
    if (orderableMissing) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING));
    }
    checkOrderableDuplication(lineItems);
    checkIncludeActiveStockCard(dto);
  }

  private void checkIncludeActiveStockCard(PhysicalInventoryDto dto) {
    List<UUID> coveredOrderableIds = dto.getLineItems().stream()
        .map(lineItemDto -> lineItemDto.getOrderable().getId()).collect(toList());

    boolean activeCardMissing = stockCardRepository
        .findByProgramIdAndFacilityId(dto.getProgramId(), dto.getFacilityId()).stream()
        .map(StockCard::getOrderableId)
        .anyMatch(cardOrderableId -> !coveredOrderableIds.contains(cardOrderableId));

    if (activeCardMissing) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD));
    }
  }

  private void checkOrderableDuplication(List<PhysicalInventoryLineItemDto> lineItems) {
    long count = lineItems.stream()
        .map(lineItem -> lineItem.getOrderable().getId()).distinct().count();
    if (count < lineItems.size()) {
      throw new ValidationMessageException(
          new Message(ERROR_PHYSICAL_INVENTORY_ORDERABLE_DUPLICATION));
    }
  }

  private PhysicalInventoryDto createEmptyInventory(UUID programId, UUID facilityId) {
    return PhysicalInventoryDto.builder()
        .programId(programId)
        .facilityId(facilityId)
        .lineItems(approvedProductsToInventoryLineItemDto(programId, facilityId))
        .build();
  }

  private List<PhysicalInventoryLineItemDto> approvedProductsToInventoryLineItemDto(
      UUID programId, UUID facilityId) {
    return approvedProductReferenceDataService
        .getAllApprovedProducts(programId, facilityId)
        .stream()
        .map(approvedProductDto -> orderableReferenceDataService
            .findOne(approvedProductDto.getProgramOrderable().getOrderableId()))
        .map(orderableDto ->
            PhysicalInventoryLineItemDto.builder().orderable(orderableDto).build())
        .collect(toList());
  }
}
