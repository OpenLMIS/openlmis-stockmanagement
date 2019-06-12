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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class CalculatedStockOnHandDtoTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private CalculatedStockOnHand domain;
  private CalculatedStockOnHandDto dto;

  @Before
  public void setUp() {
    domain = new CalculatedStockOnHandDataBuilder().build();
    dto = new CalculatedStockOnHandDto();
    domain.export(dto);
  }

  @Test
  public void shouldCreateInstanceBasedOnDomainObject() {
    CalculatedStockOnHandDto newDto = CalculatedStockOnHandDto.newInstance(domain);
    assertThat(newDto, is(dto));
  }

  @Test
  public void shouldReturnCorrectStockOnHand() {
    assertThat(dto.getStockOnHand(), is(domain.getStockOnHand()));
  }

  @Test
  public void shouldReturnCorrectDate() {
    assertThat(dto.getDate(), is(domain.getDate()));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(CalculatedStockOnHandDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(CalculatedStockOnHandDto.class, dto);
  }
}
