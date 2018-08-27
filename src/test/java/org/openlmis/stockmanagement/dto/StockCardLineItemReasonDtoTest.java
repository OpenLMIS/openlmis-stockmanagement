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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_CATEGORY_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_TYPE_INVALID;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class StockCardLineItemReasonDtoTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private StockCardLineItemReason domain = new StockCardLineItemReasonDataBuilder().build();
  private StockCardLineItemReasonDto dto = new StockCardLineItemReasonDto();

  @Before
  public void setUp() throws Exception {
    domain.export(dto);
  }

  @Test
  public void shouldCreateInstanceBasedOnDomainObject() {
    StockCardLineItemReasonDto newDto = StockCardLineItemReasonDto.newInstance(domain);
    assertThat(newDto, is(dto));
  }

  @Test
  public void shouldReturnCorrectReasonType() {
    assertThat(dto.getReasonType(), is(domain.getReasonType()));
  }

  @Test
  public void shouldThrowExceptionIfReasonTypeIsInvalid() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_REASON_TYPE_INVALID));

    dto.setType("abc");
    dto.getReasonType();
  }

  @Test
  public void shouldReturnCorrectReasonCategory() {
    assertThat(dto.getReasonCategory(), is(domain.getReasonCategory()));
  }

  @Test
  public void shouldThrowExceptionIfReasonCategoryIsInvalid() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_REASON_CATEGORY_INVALID));

    dto.setCategory("abc");
    dto.getReasonCategory();
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(StockCardLineItemReasonDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(StockCardLineItemReasonDto.class, dto);
  }
}
