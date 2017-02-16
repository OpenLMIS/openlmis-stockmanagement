package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.movement.Organization;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.OrderableDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.openlmis.stockmanagement.domain.card.StockCard.createStockCardFrom;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemsFrom;

@Service
public class StockCardService {

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Generate stock card line items and stock cards based on event, and persist them.
   *
   * @param stockEventDto the origin event.
   * @param savedEventId  saved event id.
   * @param currentUserId current user id.
   * @throws IllegalAccessException IllegalAccessException.
   * @throws InstantiationException InstantiationException.
   */
  public void saveFromEvent(StockEventDto stockEventDto, UUID savedEventId, UUID currentUserId)
          throws IllegalAccessException, InstantiationException {

    StockCard stockCard = findExistingOrCreateNewCard(stockEventDto, savedEventId);

    createLineItemsFrom(stockEventDto, stockCard, savedEventId, currentUserId);
    stockCardRepository.save(stockCard);
  }

  /**
   * Find stock card by stock card id.
   *
   * @param stockCardId stock card id.
   * @return the found stock card.
   */
  public StockCardDto findStockCardById(UUID stockCardId) {
    StockCard foundCard = stockCardRepository.findOne(stockCardId);
    if (foundCard == null) {
      return null;
    }

    permissionService.canViewStockCard(foundCard.getProgramId(), foundCard.getFacilityId());

    StockCardDto stockCardDto = createStockCardDto(foundCard);
    assignSourceDestinationForLineItems(stockCardDto);
    stockCardDto.reorderLineItemsByDates();
    stockCardDto.calculateStockOnHand();
    return stockCardDto;
  }

  private StockCardDto createStockCardDto(StockCard stockCard) {
    FacilityDto facility = facilityReferenceDataService.findOne(stockCard.getFacilityId());
    ProgramDto program = programReferenceDataService.findOne(stockCard.getProgramId());
    OrderableDto orderable = orderableReferenceDataService.findOne(stockCard.getOrderableId());

    return StockCardDto.createFrom(stockCard, facility, program, orderable);
  }

  private void assignSourceDestinationForLineItems(StockCardDto stockCardDto) {
    stockCardDto.getLineItems().forEach(lineItemDto -> {
      FacilityDto source =
              getFromRefDataOrConvertOrg(lineItemDto.getLineItem().getSource());
      FacilityDto destination =
              getFromRefDataOrConvertOrg(lineItemDto.getLineItem().getDestination());

      lineItemDto.setSource(source);
      lineItemDto.setDestination(destination);
    });
  }

  private FacilityDto getFromRefDataOrConvertOrg(Node node) {
    if (node == null) {
      return null;
    }

    if (node.isRefDataFacility()) {
      return facilityReferenceDataService.findOne(node.getReferenceId());
    } else {
      Organization organization = organizationRepository.findOne(node.getReferenceId());
      return FacilityDto.createFrom(organization);
    }
  }

  private StockCard findExistingOrCreateNewCard(StockEventDto stockEventDto, UUID savedEventId)
          throws InstantiationException, IllegalAccessException {

    StockCard foundCard = stockCardRepository.findByProgramIdAndFacilityIdAndOrderableId(
            stockEventDto.getProgramId(),
            stockEventDto.getFacilityId(),
            stockEventDto.getOrderableId());

    if (foundCard != null) {
      return foundCard;
    } else {
      return createStockCardFrom(stockEventDto, savedEventId);
    }
  }
}
