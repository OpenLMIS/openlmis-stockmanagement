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

package org.openlmis.stockmanagement.web.stockcardsummariesv2;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItems;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.web.stockcardsummariesv2.StockCardSummariesV2DtoBuilder.ORDERABLES;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.testutils.CanFulfillForMeEntryDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableFulfillDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.testutils.VersionedObjectReferenceDtoDataBuilder;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class StockCardSummariesV2DtoBuilderTest {

  @InjectMocks
  private StockCardSummariesV2DtoBuilder builder;

  private UUID programId = randomUUID();
  private UUID facilityId = randomUUID();
  private OrderableDto orderable1;
  private OrderableDto orderable2;
  private OrderableDto orderable3;
  private OrderableDto orderable4;
  private OrderableFulfillDto orderableFulfill1;
  private OrderableFulfillDto orderableFulfill2;
  private OrderableFulfillDto orderableFulfill3;
  private OrderableFulfillDto orderableFulfill4;
  private StockCard stockCard1;
  private StockCard stockCard3a;
  private StockCard stockCard3b;
  private Map<UUID, OrderableFulfillDto> fulfillMap;

  @Before
  public void before() {
    ReflectionTestUtils.setField(builder, "serviceUrl", "https://openlmis/");

    orderable1 = new OrderableDtoDataBuilder().build();
    orderable2 = new OrderableDtoDataBuilder().build();
    orderable3 = new OrderableDtoDataBuilder().build();
    orderable4 = new OrderableDtoDataBuilder().build();

    StockEvent event = new StockEventDataBuilder()
        .withFacility(facilityId)
        .withProgram(programId)
        .build();
    stockCard1 = new StockCardDataBuilder(event)
        .withStockOnHand(16)
        .withLineItem(new StockCardLineItemDataBuilder().buildWithStockOnHand(16))
        .withOrderableId(orderable1.getId())
        .build();
    stockCard3a = new StockCardDataBuilder(event)
        .withStockOnHand(30)
        .withLineItem(new StockCardLineItemDataBuilder().buildWithStockOnHand(30))
        .withOrderableId(orderable3.getId())
        .build();
    stockCard3b = new StockCardDataBuilder(event)
        .withLotId(UUID.randomUUID())
        .withUnitOfOrderableId(UUID.randomUUID())
        .withStockOnHand(10)
        .withLineItem(new StockCardLineItemDataBuilder().buildWithStockOnHand(10))
        .withOrderableId(orderable3.getId())
        .build();

    orderableFulfill1 = new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable2.getId(), orderable3.getId())).build();
    orderableFulfill2 = new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(Collections.singletonList(orderable1.getId())).build();
    orderableFulfill3 = new OrderableFulfillDtoDataBuilder().build();
    orderableFulfill4 = new OrderableFulfillDtoDataBuilder().build();

    fulfillMap = new LinkedHashMap<>();
  }

  @Test
  public void shouldBuildStockCardSummaries() {
    List<StockCard> stockCards = asList(stockCard1, stockCard3a);

    fulfillMap.put(orderable1.getId(), orderableFulfill1);
    fulfillMap.put(orderable2.getId(), orderableFulfill2);
    fulfillMap.put(orderable3.getId(), orderableFulfill3);

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable1, orderable2, orderable3),
        stockCards, fulfillMap, false);

    StockCardSummaryV2Dto summary1 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable1.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard3a, orderable3),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable1))
    );

    StockCardSummaryV2Dto summary2 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable2.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable1))
    );

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(new CanFulfillForMeEntryDtoDataBuilder()
            .buildWithStockCardAndOrderable(stockCard3a, orderable3))
    );

    assertEquals(3, result.size());
    assertThat(result, hasItems(summary1, summary2, summary3));
  }

  @Test
  public void shouldBuildStockCardSummariesWithMultipleStockCardsForOrderable() {
    List<StockCard> stockCards = asList(stockCard1, stockCard3a, stockCard3b);

    fulfillMap.put(orderable1.getId(), orderableFulfill1);
    fulfillMap.put(orderable2.getId(), orderableFulfill2);
    fulfillMap.put(orderable3.getId(), orderableFulfill3);

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable1, orderable2, orderable3),
        stockCards, fulfillMap, false);

    StockCardSummaryV2Dto summary1 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable1.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard3a, orderable3),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard3b, orderable3),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable1))
    );

    StockCardSummaryV2Dto summary2 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable2.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable1))
    );

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard3a, orderable3),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard3b, orderable3)
        )
    );

    assertEquals(3, result.size());
    assertThat(result, hasItems(summary1, summary2, summary3));
  }

  @Test
  public void shouldSortStockCardSummaries() {
    List<StockCard> stockCards = asList(stockCard1, stockCard3a, stockCard3b);

    fulfillMap.put(orderable2.getId(), orderableFulfill2);
    fulfillMap.put(orderable3.getId(), orderableFulfill3);

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable2, orderable3),
        stockCards, fulfillMap, false);

    StockCardSummaryV2Dto summary2 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable2.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable1))
    );

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard3a, orderable3),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard3b, orderable3)
        )
    );

    assertEquals(2, result.size());
    assertEquals(result.get(0), summary3);
    assertEquals(result.get(1), summary2);
  }

  @Test
  public void shouldOmitEmptySummariesIfFlagIsSet() {
    List<StockCard> stockCards = asList(stockCard1, stockCard3a);

    fulfillMap.put(orderable3.getId(), orderableFulfill3);
    fulfillMap.put(orderable4.getId(), orderableFulfill4);

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable3, orderable4),
        stockCards, fulfillMap, true);

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(new CanFulfillForMeEntryDtoDataBuilder()
            .buildWithStockCardAndOrderable(stockCard3a, orderable3))
    );

    assertEquals(1, result.size());
    assertThat(result, hasItems(summary3));
  }

  @Test
  public void shouldNotOmitEmptySummariesIfFlagIsNotSet() {
    List<StockCard> stockCards = asList(stockCard1, stockCard3a);

    fulfillMap.put(orderable3.getId(), orderableFulfill3);
    fulfillMap.put(orderable4.getId(), orderableFulfill4);

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable3, orderable4),
        stockCards, fulfillMap, false);

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(new CanFulfillForMeEntryDtoDataBuilder()
            .buildWithStockCardAndOrderable(stockCard3a, orderable3))
    );

    StockCardSummaryV2Dto summary4 = new StockCardSummaryV2Dto(
        new VersionedObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable4.getId())
            .build(),
        asSet()
    );

    assertEquals(2, result.size());
    assertThat(result, hasItems(summary3, summary4));
  }
}
