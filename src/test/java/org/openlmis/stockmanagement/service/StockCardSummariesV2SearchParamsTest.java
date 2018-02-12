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

package org.openlmis.stockmanagement.service;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;
import static org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams.AS_OF_DATE;
import static org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams.FACILITY_ID;
import static org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams.ORDERABLE_ID;
import static org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams.PAGE;
import static org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams.PROGRAM_ID;
import static org.openlmis.stockmanagement.service.StockCardSummariesV2SearchParams.SIZE;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.testutils.StockCardSummariesV2SearchParamsDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class StockCardSummariesV2SearchParamsTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void shouldThrowExceptionIfFacilityIdIsNotSet() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_FACILITY_ID_MISSING);

    new StockCardSummariesV2SearchParamsDataBuilder()
        .withoutFacilityId()
        .build()
        .validate();
  }

  @Test
  public void shouldThrowExceptionIfProgramIdIsNotSet() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_PROGRAM_ID_MISSING);

    new StockCardSummariesV2SearchParamsDataBuilder()
        .withoutProgramId()
        .build()
        .validate();
  }

  @Test
  public void shouldValidateParams() {
    new StockCardSummariesV2SearchParamsDataBuilder().build().validate();
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(StockCardSummariesV2SearchParams.class)
        .suppress(Warning.NONFINAL_FIELDS) // we can't make fields as final in DTO
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    StockCardSummariesV2SearchParams params =
        new StockCardSummariesV2SearchParamsDataBuilder().build();
    ToStringTestUtils.verify(StockCardSummariesV2SearchParams.class, params);
  }

  @Test
  public void shouldCreateParamsFromMap() {
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    LocalDate asOfDate = LocalDate.now();
    UUID orderableId1 = UUID.randomUUID();
    UUID orderableId2 = UUID.randomUUID();

    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add(PROGRAM_ID, programId.toString());
    parameters.add(FACILITY_ID, facilityId.toString());
    parameters.add(AS_OF_DATE, asOfDate.format(DateTimeFormatter.ISO_DATE));
    parameters.add(PAGE, "0");
    parameters.add(SIZE, "10");
    parameters.add(ORDERABLE_ID, orderableId1.toString());
    parameters.add(ORDERABLE_ID, orderableId2.toString());

    StockCardSummariesV2SearchParams params = new StockCardSummariesV2SearchParams(parameters);

    assertEquals(params.getProgramId(), programId);
    assertEquals(params.getFacilityId(), facilityId);
    assertEquals(params.getAsOfDate(), asOfDate);
    assertEquals(params.getPageable().getPageNumber(), 0);
    assertEquals(params.getPageable().getPageSize(), 10);
    assertEquals(params.getOrderableIds(), asList(orderableId1, orderableId2));
  }

  @Test
  public void shouldCreateEmptyParams() {
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

    StockCardSummariesV2SearchParams params = new StockCardSummariesV2SearchParams(parameters);

    assertEquals(params.getProgramId(), null);
    assertEquals(params.getFacilityId(), null);
    assertEquals(params.getAsOfDate(), null);
    assertEquals(params.getPageable().getPageNumber(), 0);
    assertEquals(params.getPageable().getPageSize(), Integer.MAX_VALUE);
    assertTrue(isEmpty(params.getOrderableIds()));
  }
}
