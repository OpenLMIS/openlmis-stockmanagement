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

import static java.time.ZonedDateTime.now;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.openlmis.stockmanagement.domain.card.StockCard.createStockCardFrom;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemFrom;
import static org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity.identityOf;
import static org.openlmis.stockmanagement.domain.reason.ReasonCategory.PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_CARDS_VIEW;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.identity.OrderableLotIdentity;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.domain.sourcedestination.Organization;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.PermissionStringDto;
import org.openlmis.stockmanagement.service.referencedata.PermissionStrings;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.web.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

/**
 * This class is in charge of persisting and retrieving stock cards. For persisting, it may create
 * and save multiple stock cards in one go, since one stock event may involve more than one
 * orderable/lot combos. For retrieving, it only retrieves one stock card at a time. Its purpose is
 * for users to view one single stock card with full details.
 */
@Service
public class StockCardService extends StockCardBaseService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StockCardService.class);
  private static final String PHYSICAL_INVENTORY_REASON_PREFIX =
      "stockmanagement.reason.physicalInventory.";

  @Autowired
  private MessageService messageService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private OrderableReferenceDataService orderableRefDataService;

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  @Autowired
  private StockCardRepository cardRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private StockOnHandCalculationService calculationSoHService;

  /**
   * Generate stock card line items and stock cards based on event, and persist them.
   *
   * @param stockEventDto the origin event.
   * @param savedEventId  saved event id.
   */
  @Transactional
  void saveFromEvent(StockEventDto stockEventDto, UUID savedEventId) {

    List<StockCard> cardsToUpdate = new ArrayList<>();
    List<StockCardLineItem> existingLineItems = new ArrayList<>();
    ZonedDateTime processedDate = now();

    for (StockEventLineItemDto eventLineItem : stockEventDto.getLineItems()) {
      StockCard stockCard = findOrCreateCard(
          stockEventDto, eventLineItem, savedEventId, cardsToUpdate);
      existingLineItems.addAll(stockCard.getLineItems());

      createLineItemFrom(stockEventDto, eventLineItem, stockCard, savedEventId, processedDate);
    }

    cardRepository.saveAll(cardsToUpdate);
    cardRepository.flush();

    calculatedStockOnHandService.recalculateStockOnHand(
        getSavedButNewLineItems(cardsToUpdate, existingLineItems));

    stockEventDto.getContext().refreshCards();

    LOGGER.debug("Stock cards and line items saved");
  }

  /**
   * Find stock card by stock card id.
   *
   * @param stockCardId stock card id.
   * @return the found stock card.
   */
  public StockCardDto findStockCardById(UUID stockCardId) {
    StockCard card = cardRepository.findById(stockCardId).orElse(null);
    if (card == null) {
      return null;
    }
    StockCard foundCard = card.shallowCopy();

    LOGGER.debug("Stock card found");
    permissionService.canViewStockCard(foundCard.getProgramId(), foundCard.getFacilityId());

    calculationSoHService.calculateStockOnHand(foundCard);

    StockCardDto cardDto = createDtos(singletonList(foundCard)).get(0);
    cardDto.setOrderable(orderableRefDataService.findOne(foundCard.getOrderableId()));
    if (cardDto.hasLot()) {
      cardDto.setLot(lotReferenceDataService.findOne(cardDto.getLot().getId()));
    }
    assignSourceDestinationReasonNameForLineItems(cardDto);
    return cardDto;
  }

  /**
   * Find stock card page by parameters. Allowed multiple id parameters.
   *
   * @param ids      collection of ids for batch fetch
   * @param pageable pagination and sorting parameters
   * @return page of filtered stock cards.
   */
  public Page<StockCardDto> search(@NotNull Collection<UUID> ids, Pageable pageable) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();
    Page<StockCard> page;
    if (!authentication.isClientOnly()) {
      UserDto user = authenticationHelper.getCurrentUser();
      LOGGER.info("list of ids:" + ids);

      PermissionStrings.Handler handler = permissionService.getPermissionStrings(user.getId());
      Set<PermissionStringDto> permissionStrings = handler.get();
      LOGGER.info("list of permission strings:" + permissionStrings);

      Set<UUID> facilityIds = new HashSet<>();
      Set<UUID> programIds = new HashSet<>();

      permissionStrings.stream()
          .filter(permissionString -> STOCK_CARDS_VIEW
              .equalsIgnoreCase(permissionString
                  .getRightName()))
          .forEach(permission -> {
            facilityIds.add(permission.getFacilityId());
            programIds.add(permission.getProgramId());
          });
      LOGGER.info("list of facility ids:" + facilityIds);
      LOGGER.info("list of program ids:" + programIds);

      if (isEmpty(ids)) {
        page = cardRepository.findByFacilityIdInAndProgramIdIn(facilityIds, programIds, pageable);
      } else {
        page = cardRepository
            .findByFacilityIdInAndProgramIdInAndIdIn(facilityIds, programIds, ids, pageable);
      }
    } else {
      if (isEmpty(ids)) {
        page = cardRepository.findAll(pageable);
      } else {
        page = cardRepository.findByIdIn(ids, pageable);
      }
    }

    return Pagination.getPage(createDtos(page.getContent()), pageable, page.getTotalElements());
  }

  /**
   * Set stock card to inactive.
   *
   * @param stockCardId      id of stockCard to update
   */
  @Transactional
  public void setInactive(UUID stockCardId) {
    cardRepository.findById(stockCardId)
        .map(stockCard -> {
          stockCard.setActive(false);
          return cardRepository.saveAndFlush(stockCard);
        })
        .orElseThrow(() ->
            new ResourceNotFoundException("Not found stock card with id: " + stockCardId));
  }

  private List<StockCardLineItem> getSavedButNewLineItems(List<StockCard> cardsToUpdate,
      List<StockCardLineItem> existingLineItems) {
    return cardsToUpdate.stream()
        .flatMap(card -> card.getLineItems().stream())
        .filter(item -> !existingLineItems.contains(item))
        .collect(Collectors.toList());
  }

  private StockCard findOrCreateCard(StockEventDto eventDto, StockEventLineItemDto eventLineItem,
      UUID savedEventId, List<StockCard> cardsToUpdate) {
    OrderableLotIdentity identity = identityOf(eventLineItem);
    StockCard card = eventDto.getContext().findCard(identity);

    if (null == card) {
      card = cardsToUpdate
          .stream()
          .filter(elem -> identityOf(elem).equals(identity))
          .findFirst()
          .orElse(null);
    }

    if (null == card) {
      card = createStockCardFrom(eventDto, eventLineItem, savedEventId);
    }

    if (cardsToUpdate.stream().noneMatch(elem -> identityOf(elem).equals(identity))) {
      cardsToUpdate.add(card);
    }

    return card;
  }

  private void assignSourceDestinationReasonNameForLineItems(StockCardDto stockCardDto) {
    stockCardDto.getLineItems().forEach(lineItemDto -> {
      StockCardLineItem lineItem = lineItemDto.getLineItem();
      assignReasonName(lineItem);
      lineItemDto.setSource(getFromRefDataOrConvertOrg(lineItem.getSource()));
      lineItemDto.setDestination(getFromRefDataOrConvertOrg(lineItem.getDestination()));
    });
  }

  private void assignReasonName(StockCardLineItem lineItem) {
    boolean isPhysicalReason = lineItem.getReason() != null
        && lineItem.getReason().getReasonCategory() == PHYSICAL_INVENTORY;
    if (isPhysicalReason) {
      String messageKey = PHYSICAL_INVENTORY_REASON_PREFIX
          + lineItem.getReason().getReasonType().toString().toLowerCase();
      String reasonName = messageService.localize(new Message(messageKey)).getMessage();
      lineItem.getReason().setName(reasonName);
    }
  }

  private FacilityDto getFromRefDataOrConvertOrg(Node node) {
    if (node == null) {
      return null;
    }

    if (node.isRefDataFacility()) {
      LOGGER.debug("Calling ref data to retrieve facility info for line item");
      return facilityRefDataService.findOne(node.getReferenceId());
    } else {
      Organization org = organizationRepository.findById(node.getReferenceId()).orElse(null);
      if (null != org) {
        return FacilityDto.createFrom(org);
      } else {
        LOGGER.warn("Could not find any organization matching node id {}", node.getReferenceId());
        return FacilityDto.createFrom(new Organization());
      }
    }
  }
}
