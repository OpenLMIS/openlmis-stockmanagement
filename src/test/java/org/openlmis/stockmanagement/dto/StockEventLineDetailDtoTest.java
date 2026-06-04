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

package org.openlmis.stockmanagement.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.testutils.OrderableDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class StockEventLineDetailDtoTest {

  @Test
  public void newInstanceShouldFlattenCardProductAndLineItemValues() {
    OrderableDto orderable = OrderableDto.builder().productCode("ABC01").build();
    FacilityDto source = FacilityDto.builder().name("Provincial WH").build();
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().build();
    StockCardLineItem item = new StockCardLineItemDataBuilder()
        .withQuantity(7)
        .withStockOnHand(20)
        .withOccurredDate(LocalDate.of(2026, 2, 15))
        .withReason(reason)
        .withDocumentNumber("2026-02-FAC001-0001")
        .build();

    StockCardDto card = StockCardDto.builder().orderable(orderable).build();
    StockCardLineItemDto lineItemDto = StockCardLineItemDto.builder()
        .lineItem(item)
        .source(source)
        .build();

    StockEventLineDetailDto dto = StockEventLineDetailDto.newInstance(card, lineItemDto);

    assertThat(dto.getOrderable(), is(orderable));
    assertThat(dto.getSource(), is(source));
    assertThat(dto.getQuantity(), is(7));
    assertThat(dto.getStockOnHand(), is(20));
    assertThat(dto.getOccurredDate(), is(LocalDate.of(2026, 2, 15)));
    assertThat(dto.getReason(), is(reason));
    assertThat(dto.getDocumentNumber(), is("2026-02-FAC001-0001"));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(StockEventLineDetailDto.class)
        .withPrefabValues(OrderableDto.class,
            new OrderableDtoDataBuilder().build(), new OrderableDtoDataBuilder().build())
        .withPrefabValues(FacilityDto.class,
            FacilityDto.builder().id(UUID.randomUUID()).build(),
            FacilityDto.builder().id(UUID.randomUUID()).build())
        .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    StockEventLineDetailDto dto = new StockEventLineDetailDto();
    ToStringTestUtils.verify(StockEventLineDetailDto.class, dto);
  }
}
