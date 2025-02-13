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

package org.openlmis.stockmanagement.web.external.stockcardsummaries;

import static java.util.Collections.emptyList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_CODE_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_CODE_PARAM_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_CODE_PARAM_MORE_THEN_ONE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_CODE_NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.StockCardSummaries;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.web.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/stockCardSummaries")
public class StockCardSummariesExternalController {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(StockCardSummariesExternalController.class);

  static final String FACILITY_CODE_PARAM_NAME = "facility";
  static final String PROGRAM_CODE_PARAM_NAME = "program";
  static final String ORDERABLE_CODE_PARAM_NAME = "code";

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  @Autowired
  private StockCardSummariesExternalDtoBuilder stockCardSummariesExternalDtoBuilder;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  /**
   * Get Stock Card Summaries for external integrations.
   *
   * @param parameters the request query parameters, not null
   * @param pageable   the page request, not null
   * @return the requested page of Stock Card summaries, never null
   */
  @GetMapping
  public Page<StockCardSummaryExternalDto> getStockCardSummaries(
      @RequestParam MultiValueMap<String, String> parameters,
      @PageableDefault(size = Integer.MAX_VALUE) Pageable pageable) {
    Profiler profiler = new Profiler("GET_STOCK_CARDS_EXTERNAL");
    profiler.setLogger(LOGGER);

    profiler.start("VALIDATE_PARAMS");
    StockCardSummariesV2SearchParams params = buildParams(parameters);

    profiler.start("GET_STOCK_CARD_SUMMARIES_FOR_EXTERNAL");
    StockCardSummaries summaries = stockCardSummariesService.findStockCards(params);

    List<StockCardSummaryExternalDto> dtos = stockCardSummariesExternalDtoBuilder
        .build(summaries.getApprovedProducts(), summaries.getStockCardsForFulfillOrderables(),
            summaries.getOrderableFulfillMap(), params.isNonEmptyOnly(),
            profiler.startNested("BUILD_DTOS"));

    profiler.start("GET_PAGE");
    Page<StockCardSummaryExternalDto> page = Pagination.getPage(dtos, pageable);

    profiler.stop().log();
    return page;
  }

  private StockCardSummariesV2SearchParams buildParams(MultiValueMap<String, String> parameters) {
    final FacilityDto facility = getFacility(parameters);
    final List<UUID> programIds = getProgramIds(parameters);

    return new StockCardSummariesV2SearchParams(programIds, facility.getId(), null, null, true,
        parameters.getFirst(ORDERABLE_CODE_PARAM_NAME), null, null);
  }

  private FacilityDto getFacility(MultiValueMap<String, String> requestParameters) {
    if (requestParameters.getFirst(FACILITY_CODE_PARAM_NAME) == null) {
      throw new ValidationMessageException(ERROR_FACILITY_CODE_PARAM_MISSING);
    } else if (requestParameters.get(FACILITY_CODE_PARAM_NAME) != null
        && requestParameters.get(FACILITY_CODE_PARAM_NAME).size() > 1) {
      throw new ValidationMessageException(ERROR_FACILITY_CODE_PARAM_MORE_THEN_ONE);
    }

    final String facilityCode = requestParameters.getFirst(FACILITY_CODE_PARAM_NAME);
    return facilityReferenceDataService.findByCode(facilityCode).orElseThrow(
        () -> new ValidationMessageException(
            new Message(ERROR_FACILITY_CODE_NOT_FOUND, facilityCode)));
  }

  private List<UUID> getProgramIds(MultiValueMap<String, String> requestParameters) {
    final List<UUID> result = new ArrayList<>();

    for (String programCode : requestParameters
        .getOrDefault(PROGRAM_CODE_PARAM_NAME, emptyList())) {
      final Optional<ProgramDto> program = programReferenceDataService.findByCode(programCode);

      result.add(program.orElseThrow(() -> new ValidationMessageException(
          new Message(ERROR_PROGRAM_CODE_NOT_FOUND, programCode))).getId());
    }

    return result;
  }
}
