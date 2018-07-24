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
import static org.openlmis.stockmanagement.testutils.DatesUtil.getBaseDate;
import static org.openlmis.stockmanagement.web.stockcardsummariesv2.StockCardSummariesV2DtoBuilder.ORDERABLES;

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
import org.openlmis.stockmanagement.testutils.ObjectReferenceDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.OrderableFulfillDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
  private StockCard stockCard;
  private StockCard stockCard1;
  private StockCard stockCard2;
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
    stockCard = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(12,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(16),
            orderable1.getId());
    stockCard1 = new StockCardDataBuilder(event)
        .buildWithStockOnHandAndLineItemAndOrderableId(26,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(30),
            orderable3.getId());
    stockCard2 = new StockCardDataBuilder(event)
        .withLot(UUID.randomUUID())
        .buildWithStockOnHandAndLineItemAndOrderableId(22,
            new StockCardLineItemDataBuilder().buildWithStockOnHand(10),
            orderable3.getId());

    fulfillMap = new HashMap<>();
    fulfillMap.put(orderable1.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(asList(orderable2.getId(), orderable3.getId())).build());
    fulfillMap.put(orderable2.getId(), new OrderableFulfillDtoDataBuilder()
        .withCanFulfillForMe(Collections.singletonList(orderable1.getId())).build());
  }

  @Test
  public void shouldBuildStockCardSummaries() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable1, orderable2, orderable3),
        stockCards, fulfillMap, asOfDate);

    StockCardSummaryV2Dto summary1 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable1.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard, orderable1, asOfDate))
    );

    StockCardSummaryV2Dto summary2 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable2.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard, orderable1, asOfDate))
    );

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(new CanFulfillForMeEntryDtoDataBuilder()
            .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate))
    );

    assertEquals(3, result.size());
    assertThat(result, hasItems(summary1, summary2, summary3));
  }

  @Test
  public void shouldBuildStockCardSummariesWithMultipleStockCardsForOrderable() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1, stockCard2);

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable1, orderable2, orderable3),
        stockCards, fulfillMap, asOfDate);

    StockCardSummaryV2Dto summary1 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable1.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard2, orderable3, asOfDate),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard, orderable1, asOfDate))
    );

    StockCardSummaryV2Dto summary2 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable2.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard, orderable1, asOfDate))
    );

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard2, orderable3, asOfDate)
        )
    );

    assertEquals(3, result.size());
    assertThat(result, hasItems(summary1, summary2, summary3));
  }

  @Test
  public void shouldSortStockCardSummaries() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1, stockCard2);

    fulfillMap.remove(orderable1.getId());

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable2, orderable3),
        stockCards, fulfillMap, asOfDate);

    StockCardSummaryV2Dto summary2 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable2.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard, orderable1, asOfDate))
    );

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard2, orderable3, asOfDate)
        )
    );

    assertEquals(2, result.size());
    assertEquals(result.get(0), summary3);
    assertEquals(result.get(1), summary2);
  }

  @Test
  public void shouldBuildStockCardSummariesWithDateBeforeThereWereCards() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    LocalDate asOfDate = getBaseDate().minusDays(10);

    List<StockCardSummaryV2Dto> result = builder
        .nonEmptySummariesOnly()
        .build(Collections.singletonList(orderable1), stockCards, fulfillMap, asOfDate);

    StockCardSummaryV2Dto summary1 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable1.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard, orderable1, asOfDate))
    );

    assertEquals(1, result.size());
    assertThat(result, hasItems(summary1));
  }

  @Test
  public void shouldBuildStockCardSummariesForCurrentDate() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(Collections.singletonList(orderable1),
        stockCards, fulfillMap, null);

    StockCardSummaryV2Dto summary1 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable1.getId())
            .build(),
        asSet(
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate),
            new CanFulfillForMeEntryDtoDataBuilder()
                .buildWithStockCardAndOrderable(stockCard, orderable1, asOfDate))
    );

    assertEquals(1, result.size());
    assertThat(result, hasItems(summary1));
  }

  @Test
  public void shouldOmitEmptySummariesIfFlagIsSet() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder
        .nonEmptySummariesOnly()
        .build(asList(orderable3, orderable4), stockCards, fulfillMap, asOfDate);

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(new CanFulfillForMeEntryDtoDataBuilder()
            .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate))
    );

    assertEquals(1, result.size());
    assertThat(result, hasItems(summary3));
  }

  @Test
  public void shouldNotOmitEmptySummariesIfFlagIsNotSet() throws Exception {
    List<StockCard> stockCards = asList(stockCard, stockCard1);

    LocalDate asOfDate = LocalDate.now();

    List<StockCardSummaryV2Dto> result = builder.build(asList(orderable3, orderable4),
        stockCards, fulfillMap, asOfDate);

    StockCardSummaryV2Dto summary3 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable3.getId())
            .build(),
        asSet(new CanFulfillForMeEntryDtoDataBuilder()
            .buildWithStockCardAndOrderable(stockCard1, orderable3, asOfDate))
    );

    StockCardSummaryV2Dto summary4 = new StockCardSummaryV2Dto(
        new ObjectReferenceDtoDataBuilder()
            .withPath(ORDERABLES)
            .withId(orderable4.getId())
            .build(),
        asSet()
    );

    assertEquals(2, result.size());
    assertThat(result, hasItems(summary3, summary4));
  }
}
