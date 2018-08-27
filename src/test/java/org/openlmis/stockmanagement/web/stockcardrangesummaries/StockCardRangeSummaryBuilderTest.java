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

package org.openlmis.stockmanagement.web.stockcardrangesummaries;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.service.StockCardAggregate;
import org.openlmis.stockmanagement.testutils.ObjectReferenceDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardRangeSummaryDtoDataBuilder;
import org.openlmis.stockmanagement.web.stockcardrangesummary.StockCardRangeSummaryBuilder;
import org.openlmis.stockmanagement.web.stockcardrangesummary.StockCardRangeSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class StockCardRangeSummaryBuilderTest {

  private static final String ORDERABLES = "orderables";

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @InjectMocks
  private StockCardRangeSummaryBuilder builder;

  private UUID orderableId1 = randomUUID();
  private UUID orderableId2 = randomUUID();
  private Pageable pageable;
  private LocalDate endDate;
  private LocalDate startDate;
  private Map<UUID, StockCardAggregate> groupedStockCards;
  private String tag1 = "tag1";
  private String tag2 = "tag2";

  @Mock
  private StockCardAggregate aggregate1;

  @Mock
  private StockCardAggregate aggregate2;

  @Mock
  private StockCardAggregate aggregate3;

  @Before
  public void before() {
    ReflectionTestUtils.setField(builder, "serviceUrl", "https://openlmis/");

    endDate = LocalDate.now();
    startDate = LocalDate.now().minusDays(30);

    when(aggregate1.getStockoutDays(startDate, endDate))
        .thenReturn(1L);
    when(aggregate2.getStockoutDays(startDate, endDate))
        .thenReturn(2L);

    groupedStockCards = ImmutableMap.of(orderableId1, aggregate1, orderableId2, aggregate2);

    pageable = new PageRequest(0, 10);

    when(reasonRepository.existsByTag(any())).thenReturn(true);
  }

  @Test
  public void shouldBuildStockCardRangeSummariesWithTag() {
    when(aggregate1.getAmount(tag1, startDate, endDate))
        .thenReturn(10);
    when(aggregate2.getAmount(tag1, startDate, endDate))
        .thenReturn(20);

    Page<StockCardRangeSummaryDto> result = builder.build(
        groupedStockCards,
        tag1,
        startDate,
        endDate,
        pageable);

    StockCardRangeSummaryDto rangeSummary1 = new StockCardRangeSummaryDtoDataBuilder()
        .withOrderable(new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderableId1)
            .build())
        .withStockOutDays(1L)
        .withTags(ImmutableMap.of(tag1, 10))
        .build();

    StockCardRangeSummaryDto rangeSummary2 = new StockCardRangeSummaryDtoDataBuilder()
        .withOrderable(new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderableId2)
            .build())
        .withStockOutDays(2L)
        .withTags(ImmutableMap.of(tag1, 20))
        .build();

    assertEquals(2, result.getContent().size());
    assertThat(result.getContent(), hasItems(rangeSummary1, rangeSummary2));
  }

  @Test
  public void shouldBuildStockCardRangeSummariesWithoutTag() {
    when(aggregate1.getAmounts(startDate, endDate))
        .thenReturn(ImmutableMap.of(tag1, 10, tag2, 11));
    when(aggregate2.getAmounts(startDate, endDate))
        .thenReturn(ImmutableMap.of(tag1, 20));

    Page<StockCardRangeSummaryDto> result = builder.build(
        groupedStockCards,
        null,
        startDate,
        endDate,
        pageable);

    StockCardRangeSummaryDto rangeSummary1 = new StockCardRangeSummaryDtoDataBuilder()
        .withOrderable(new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderableId1)
            .build())
        .withStockOutDays(1L)
        .withTags(ImmutableMap.of(tag1, 10, tag2, 11))
        .build();

    StockCardRangeSummaryDto rangeSummary2 = new StockCardRangeSummaryDtoDataBuilder()
        .withOrderable(new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderableId2)
            .build())
        .withStockOutDays(2L)
        .withTags(ImmutableMap.of(tag1, 20))
        .build();

    assertEquals(2, result.getContent().size());
    assertThat(result.getContent(), hasItems(rangeSummary1, rangeSummary2));
  }

  @Test
  public void shouldBuildPaginatedStockCardRangeSummaries() {
    final UUID orderableId3 = randomUUID();

    String tag = "tag";

    when(aggregate1.getAmount(tag, startDate, endDate))
        .thenReturn(10);
    when(aggregate2.getAmount(tag, startDate, endDate))
        .thenReturn(20);
    when(aggregate3.getAmount(tag, startDate, endDate))
        .thenReturn(30);

    when(aggregate3.getStockoutDays(startDate, endDate))
        .thenReturn(3L);

    groupedStockCards = ImmutableMap.of(
        orderableId1, aggregate1,
        orderableId2, aggregate2,
        orderableId3, aggregate3);

    pageable = new PageRequest(1, 1);

    Page<StockCardRangeSummaryDto> result = builder.build(
        groupedStockCards,
        tag,
        startDate,
        endDate,
        pageable);

    assertEquals(1, result.getContent().size());
  }

  @Test
  public void shouldNotIncludeNotExistingTag() {
    final UUID orderableId3 = randomUUID();

    when(reasonRepository.existsByTag(tag2)).thenReturn(false);

    when(aggregate1.getAmount(tag2, startDate, endDate))
        .thenReturn(10);
    when(aggregate2.getAmount(tag2, startDate, endDate))
        .thenReturn(20);
    when(aggregate3.getAmount(tag2, startDate, endDate))
        .thenReturn(30);

    when(aggregate3.getStockoutDays(startDate, endDate))
        .thenReturn(3L);

    groupedStockCards = ImmutableMap.of(
        orderableId1, aggregate1,
        orderableId2, aggregate2,
        orderableId3, aggregate3);

    pageable = new PageRequest(1, 1);

    Page<StockCardRangeSummaryDto> result = builder.build(
        groupedStockCards,
        tag2,
        startDate,
        endDate,
        pageable);

    assertEquals(1, result.getContent().size());
    assertNull(result.getContent().get(0).getTags().get(tag2));
  }
}
