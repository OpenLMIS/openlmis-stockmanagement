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

package org.openlmis.stockmanagement.web.external.stockevents;

import static java.util.stream.Collectors.toList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_CODE_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_CODE_NOT_FOUND;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.stockmanagement.dto.StockCardLineItemReasonDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.deferredloading.LotByCodeDeferredLoader;
import org.openlmis.stockmanagement.util.deferredloading.OrderableByCodeDeferredLoader;
import org.openlmis.stockmanagement.util.deferredloading.ReasonByNameDeferredLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Component;

/**
 * Builds {@link org.openlmis.stockmanagement.dto.StockEventDto} out of
 * {@link StockEventExternalDto}.
 */
@Component
public class StockEventsAdapterBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventsAdapterBuilder.class);

  private final FacilityReferenceDataService facilityReferenceDataService;
  private final ProgramReferenceDataService programReferenceDataService;
  private final OrderableReferenceDataService orderableReferenceDataService;
  private final LotReferenceDataService lotReferenceDataService;
  private final StockCardLineItemReasonRepository reasonRepository;

  /**
   * Initialize the bean.
   *
   * @param facilityReferenceDataService  the facility reference data service
   * @param programReferenceDataService   the program reference data service
   * @param orderableReferenceDataService the orderable reference data service
   * @param lotReferenceDataService       the lot reference data service
   * @param reasonRepository              the reason repository
   */
  @Inject
  public StockEventsAdapterBuilder(FacilityReferenceDataService facilityReferenceDataService,
      ProgramReferenceDataService programReferenceDataService,
      OrderableReferenceDataService orderableReferenceDataService,
      LotReferenceDataService lotReferenceDataService,
      StockCardLineItemReasonRepository reasonRepository) {
    this.facilityReferenceDataService = facilityReferenceDataService;
    this.programReferenceDataService = programReferenceDataService;
    this.orderableReferenceDataService = orderableReferenceDataService;
    this.lotReferenceDataService = lotReferenceDataService;
    this.reasonRepository = reasonRepository;
  }

  /**
   * Build the StockEventDto from StockEventExternalDto.
   *
   * @param externalDto the dto, not null
   * @return the StockEventDto, never null
   */
  public StockEventDto build(StockEventExternalDto externalDto) {
    Profiler profiler = new Profiler("CONVERT_STOCK_EVENT_EXTERNAL_DTO_TO_REGULAR_DTO");
    profiler.setLogger(LOGGER);

    final FacilityDto facility = facilityReferenceDataService.findByCode(externalDto.getFacility())
        .orElseThrow(() -> new ValidationMessageException(
            new Message(ERROR_FACILITY_CODE_NOT_FOUND, externalDto.getFacility())));

    final ProgramDto program = programReferenceDataService.findByCode(externalDto.getProgram())
        .orElseThrow(() -> new ValidationMessageException(
            new Message(ERROR_PROGRAM_CODE_NOT_FOUND, externalDto.getProgram())));

    final List<StockEventLineItemDto> lineItems =
        createAllStockEventLineItemDtos(externalDto, profiler.startNested("CREATE_LINE_ITEMS"));

    profiler.stop().log();
    return new StockEventDto(null, facility.getId(), program.getId(), externalDto.getSignature(),
        externalDto.getDocumentNumber(), null, true, lineItems, null);
  }

  private List<StockEventLineItemDto> createAllStockEventLineItemDtos(
      StockEventExternalDto externalDto, Profiler profiler) {
    final OrderableByCodeDeferredLoader orderableLoader =
        new OrderableByCodeDeferredLoader(orderableReferenceDataService);
    final LotByCodeDeferredLoader lotLoader = new LotByCodeDeferredLoader(lotReferenceDataService);
    final ReasonByNameDeferredLoader reasonLoader =
        new ReasonByNameDeferredLoader(reasonRepository);

    final List<DeferredLineItem> deferredLineItems = new ArrayList<>();

    profiler.start("PREPARE_DEFERRED");
    for (StockEventLineItemExternalDto item : externalDto.getItems()) {
      deferredLineItems.add(new DeferredLineItem(orderableLoader.deferredLoad(item.getOrderable()),
          StringUtils.isBlank(item.getLot()) ? null : lotLoader.deferredLoad(item.getLot()),
          item.getOccurredDate(), item.getQuantity(), reasonLoader.deferredLoad(item.getReason())));
    }

    profiler.start("LOAD_DEFERRED");
    orderableLoader.loadDeferredObjects();
    lotLoader.loadDeferredObjects();
    reasonLoader.loadDeferredObjects();

    profiler.start("MAP_TO_DTO");
    return deferredLineItems.stream().map(loadedLineItem -> StockEventLineItemDto.builder()
        .orderableId(loadedLineItem.getOrderable().getId())
        .lotId(loadedLineItem.getLot().map(LotDto::getId).orElse(null))
        .occurredDate(loadedLineItem.getOccurredDate()).quantity(loadedLineItem.getQuantity())
        .reasonId(loadedLineItem.getReason().getId()).build()).collect(toList());
  }

  @AllArgsConstructor
  private static class DeferredLineItem {
    private final OrderableByCodeDeferredLoader.Handle orderable;
    private final LotByCodeDeferredLoader.Handle lot;
    @Getter
    private final LocalDate occurredDate;
    @Getter
    private final Integer quantity;
    private final ReasonByNameDeferredLoader.Handle reason;

    OrderableDto getOrderable() {
      return orderable.get();
    }

    Optional<LotDto> getLot() {
      return Optional.ofNullable(lot).map(LotByCodeDeferredLoader.Handle::get);
    }

    StockCardLineItemReasonDto getReason() {
      return reason.get();
    }
  }
}
