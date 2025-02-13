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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.service.StockCardSummaries;
import org.openlmis.stockmanagement.service.StockCardSummariesService;
import org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.slf4j.profiler.Profiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesExternalControllerTest {
  private static final String TEST_FACILITY_CODE = "FBI";
  private static final String TEST_PROGRAM_CODE = "CIA";
  private static final String TEST_ORDERABLE_CODE = "DEA";
  @Captor
  protected ArgumentCaptor<StockCardSummariesV2SearchParams> paramsCaptor;
  @Mock
  private StockCardSummariesService stockCardSummariesService;
  @Mock
  private StockCardSummariesExternalDtoBuilder stockCardSummariesExternalDtoBuilder;
  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;
  @Mock
  private ProgramReferenceDataService programReferenceDataService;
  @InjectMocks
  private StockCardSummariesExternalController controller;

  @Test
  public void getStockCardSummariesShouldReturnResult() {
    final FacilityDto facility =
        FacilityDto.builder().id(UUID.randomUUID()).code(TEST_FACILITY_CODE).build();
    final ProgramDto program =
        ProgramDto.builder().id(UUID.randomUUID()).code(TEST_PROGRAM_CODE).build();
    final OrderableDto orderable =
        OrderableDto.builder().id(UUID.randomUUID()).productCode(TEST_ORDERABLE_CODE).build();

    final PageRequest pageRequest = PageRequest.of(0, 10);
    final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters
        .add(StockCardSummariesExternalController.FACILITY_CODE_PARAM_NAME, facility.getCode());
    parameters.add(StockCardSummariesExternalController.PROGRAM_CODE_PARAM_NAME, program.getCode());
    parameters.add(StockCardSummariesExternalController.ORDERABLE_CODE_PARAM_NAME,
        orderable.getProductCode());

    final StockCardSummaries summaries = new StockCardSummaries();
    summaries.setApprovedProducts(new ArrayList<>());
    summaries.setStockCardsForFulfillOrderables(new ArrayList<>());
    summaries.setOrderableFulfillMap(new HashMap<>());
    summaries.setAsOfDate(LocalDate.now());
    summaries.setTotalElements(1L);

    when(facilityReferenceDataService.findByCode(facility.getCode()))
        .thenReturn(Optional.of(facility));
    when(programReferenceDataService.findByCode(program.getCode()))
        .thenReturn(Optional.of(program));
    when(stockCardSummariesService.findStockCards(any(StockCardSummariesV2SearchParams.class)))
        .thenReturn(summaries);
    when(stockCardSummariesExternalDtoBuilder.build(any(), any(), any(), anyBoolean(), any()))
        .thenReturn(singletonList(new StockCardSummaryExternalDto()));

    final Page<StockCardSummaryExternalDto> result = controller.getStockCardSummaries(parameters,
        pageRequest);
    assertEquals(1, result.getTotalElements());

    verify(stockCardSummariesService, only()).findStockCards(paramsCaptor.capture());
    final StockCardSummariesV2SearchParams capturedParams = paramsCaptor.getValue();

    assertEquals(facility.getId(), capturedParams.getFacilityId());
    assertTrue(capturedParams.getProgramIds().contains(program.getId()));
    assertEquals(orderable.getProductCode(), capturedParams.getOrderableCode());

    verify(stockCardSummariesExternalDtoBuilder, only()).build(eq(summaries.getApprovedProducts()),
        eq(summaries.getStockCardsForFulfillOrderables()), eq(summaries.getOrderableFulfillMap()),
        eq(capturedParams.isNonEmptyOnly()), any(Profiler.class));
  }
}
