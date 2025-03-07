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

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class StockEventsAdapterBuilderTest {
  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;
  @Mock
  private ProgramReferenceDataService programReferenceDataService;
  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;
  @Mock
  private LotReferenceDataService lotReferenceDataService;
  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @Test
  public void shouldCreateStockEventDto() {
    final FacilityDto testFacility = FacilityDto.builder().code("TestFacility1").build();
    final ProgramDto testProgram = ProgramDto.builder().code("TestProgram1").build();
    final LotDto testLot = LotDto.builder().lotCode("TestLot").build();
    final OrderableDto testOrderable = OrderableDto.builder().productCode("TestOrderable").build();
    final StockCardLineItemReason testReason = StockCardLineItemReason.builder().name("TestReason")
        .reasonCategory(ReasonCategory.ADJUSTMENT).reasonType(ReasonType.BALANCE_ADJUSTMENT)
        .build();

    when(facilityReferenceDataService.findByCode(testFacility.getCode()))
        .thenReturn(of(testFacility));
    when(programReferenceDataService.findByCode(testProgram.getCode())).thenReturn(of(testProgram));
    when(lotReferenceDataService.findByExactCodes(singleton(testLot.getLotCode())))
        .thenReturn(singletonList(testLot));
    when(orderableReferenceDataService.findByExactCodes(singleton(testOrderable.getProductCode())))
        .thenReturn(singletonList(testOrderable));
    when(reasonRepository.findByNameIn(singleton(testReason.getName())))
        .thenReturn(singletonList(testReason));

    final StockEventLineItemExternalDto testItemDto = new StockEventLineItemExternalDto();
    testItemDto.setLot(testLot.getLotCode());
    testItemDto.setOrderable(testOrderable.getProductCode());
    testItemDto.setReason(testReason.getName());
    testItemDto.setOccurredDate(LocalDate.of(2025, 1, 1));
    testItemDto.setQuantity(13);

    final StockEventExternalDto testDto = new StockEventExternalDto();
    testDto.setFacility(testFacility.getCode());
    testDto.setProgram(testProgram.getCode());
    testDto.setDocumentNumber("TestNumber");
    testDto.setSignature("TestSignature");
    testDto.setItems(singletonList(testItemDto));

    final StockEventsAdapterBuilder stockEventsAdapterBuilder =
        new StockEventsAdapterBuilder(facilityReferenceDataService, programReferenceDataService,
            orderableReferenceDataService, lotReferenceDataService, reasonRepository);

    final StockEventDto result = stockEventsAdapterBuilder.build(testDto);

    assertEquals(testFacility.getId(), result.getFacilityId());
    assertEquals(testProgram.getId(), result.getProgramId());
    assertEquals(testDto.getDocumentNumber(), result.getDocumentNumber());
    assertEquals(testDto.getSignature(), result.getSignature());
    assertNotNull(result.getLineItems());
    assertEquals(1, result.getLineItems().size());

    final StockEventLineItemDto resultItemDto = result.getLineItems().get(0);
    assertEquals(testLot.getId(), resultItemDto.getLotId());
    assertEquals(testOrderable.getId(), resultItemDto.getOrderableId());
    assertEquals(testReason.getId(), resultItemDto.getReasonId());
    assertEquals(testItemDto.getQuantity(), resultItemDto.getQuantity());
    assertEquals(testItemDto.getOccurredDate(), resultItemDto.getOccurredDate());
  }
}
