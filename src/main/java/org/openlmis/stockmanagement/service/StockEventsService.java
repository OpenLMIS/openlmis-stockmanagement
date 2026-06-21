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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventHistoryDto;
import org.openlmis.stockmanagement.dto.StockEventLineDetailDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.repository.custom.StockEventLineItemAggregate;
import org.openlmis.stockmanagement.repository.custom.StockEventSearchParams;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.web.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class StockEventsService {

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private StockCardLineItemRepository stockCardLineItemRepository;

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Searches issue/receive stock events matching the params and maps them to history rows.
   */
  public Page<StockEventHistoryDto> search(StockEventSearchParams params, Pageable pageable) {
    Page<StockEvent> page = stockEventsRepository.search(params, pageable);
    List<StockEvent> events = page.getContent();

    Map<UUID, StockEventLineItemAggregate> aggregates = aggregateLineItems(events);

    List<StockEventHistoryDto> dtos = events.stream()
        .map(event -> toHistoryDto(event, aggregates.get(event.getId())))
        .collect(Collectors.toList());
    populateUsernames(dtos);

    return new PageImpl<>(dtos, pageable, page.getTotalElements());
  }

  private Map<UUID, StockEventLineItemAggregate> aggregateLineItems(List<StockEvent> events) {
    Set<UUID> eventIds = events.stream()
        .map(StockEvent::getId)
        .collect(Collectors.toSet());

    if (eventIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return stockEventsRepository.aggregateLineItemsByEventIds(eventIds).stream()
        .collect(Collectors.toMap(
            StockEventLineItemAggregate::getStockEventId, Function.identity()));
  }

  private StockEventHistoryDto toHistoryDto(StockEvent event,
      StockEventLineItemAggregate aggregate) {
    StockEventHistoryDto dto = StockEventHistoryDto.newInstance(event);
    if (aggregate == null) {
      dto.setEntriesCount(0);
    } else {
      dto.setEntriesCount(aggregate.getEntriesCount());
      dto.setOccurredDate(aggregate.getOccurredDate());
    }
    return dto;
  }

  private void populateUsernames(List<StockEventHistoryDto> dtos) {
    Set<UUID> userIds = dtos.stream()
        .map(StockEventHistoryDto::getUserId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    if (userIds.isEmpty()) {
      return;
    }

    Map<UUID, String> usernames = userReferenceDataService.findUsersByIds(userIds).stream()
        .collect(Collectors.toMap(UserDto::getId, UserDto::getUsername));

    dtos.forEach(dto -> dto.setUsername(usernames.get(dto.getUserId())));
  }

  /**
   * Returns the line items of a single stock event (the transaction detail view), with stock
   * on hand and resolved names, paginated.
   *
   * @param stockEventId the stock event id.
   * @param pageable     pagination.
   * @return a page of detail rows.
   */
  public Page<StockEventLineDetailDto> findStockEventLineItems(UUID stockEventId,
      Pageable pageable) {
    StockEvent event = stockEventsRepository.findById(stockEventId)
        .orElseThrow(() -> new ResourceNotFoundException(
            new Message(MessageKeys.ERROR_STOCK_EVENT_NOT_FOUND, stockEventId)));

    permissionService.canViewStockCard(event.getProgramId(), event.getFacilityId());

    List<UUID> cardIds = stockCardLineItemRepository.findStockCardIdsByOriginEvent(stockEventId);

    // Cards are resolved in one batch (stock on hand and names), then ordered by id so the
    // flattened line items have a stable order before in-memory pagination (findAllById gives no
    // ordering guarantee). Only the line items belonging to this event are kept.
    List<StockEventLineDetailDto> details = new ArrayList<>();
    stockCardService.findStockCardsByIds(cardIds).stream()
        .sorted(Comparator.comparing(StockCardDto::getId))
        .forEach(card -> card.getLineItems().stream()
            .filter(lineItem -> stockEventId.equals(lineItem.getOriginEventId()))
            .forEach(lineItem ->
                details.add(StockEventLineDetailDto.newInstance(card, lineItem))));

    return Pagination.getPage(details, pageable);
  }
}
