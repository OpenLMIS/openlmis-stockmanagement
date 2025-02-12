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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.referencedata.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.slf4j.profiler.Profiler;

@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesExternalDtoBuilderTest {
  @Mock
  private LotReferenceDataService lotReferenceDataService;
  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;
  @Mock
  private ProgramReferenceDataService programReferenceDataService;
  @Mock
  private Profiler profiler;

  @Test
  public void shouldBuildStockCardSummaries() {
    final TestContext testContext = prepareTestContext();

    when(orderableReferenceDataService.findByIds(anyCollection())).thenAnswer(invocation -> {
      final Collection<UUID> idsToFind = invocation.getArgument(0);
      return testContext.orderables.stream().filter(o -> idsToFind.contains(o.getId()))
          .collect(toList());
    });
    when(programReferenceDataService.findByIds(anyCollection())).thenAnswer(invocation -> {
      final Collection<UUID> idsToFind = invocation.getArgument(0);
      return testContext.programs.stream().filter(p -> idsToFind.contains(p.getId()))
          .collect(toList());
    });

    final StockCardSummariesExternalDtoBuilder builder =
        new StockCardSummariesExternalDtoBuilder(lotReferenceDataService,
            orderableReferenceDataService, programReferenceDataService);

    final List<StockCardSummaryExternalDto> result = builder
        .build(testContext.approvedProducts, testContext.stockCards, testContext.orderableFulfills,
            true, profiler);

    assertNotNull(result);
    assertEquals(testContext.orderables.size(), result.size());
  }

  private TestContext prepareTestContext() {
    final FacilityTypeDto facilityTypeDto = FacilityTypeDto.builder().build();
    final ProgramDto programDto =
        ProgramDto.builder().id(UUID.fromString("c0c6f200-bf3a-4e38-b3ff-7e21777e2136"))
            .code("bf3a").build();
    final StockEvent stockEvent =
        new StockEventDataBuilder().withProgram(programDto.getId()).build();

    final List<OrderableDto> orderables =
        range(1, 3).mapToObj(i -> new OrderableDtoDataBuilder().build()).collect(toList());

    final List<ApprovedProductDto> approvedProducts = orderables.stream().map(
        o -> ApprovedProductDto.builder().facilityType(facilityTypeDto).program(programDto)
            .orderable(o).build()).collect(toList());

    final List<StockCard> stockCards = orderables.stream().map(
        o -> new StockCardDataBuilder(stockEvent).withLotId(null).withOrderableId(o.getId())
            .withStockOnHand(3).build()).collect(toList());

    return new TestContext(singletonList(programDto), orderables, approvedProducts, stockCards,
        emptyMap());
  }

  @AllArgsConstructor
  private static class TestContext {
    final List<ProgramDto> programs;
    final List<OrderableDto> orderables;
    final List<ApprovedProductDto> approvedProducts;
    final List<StockCard> stockCards;
    final Map<UUID, OrderableFulfillDto> orderableFulfills;
  }
}
