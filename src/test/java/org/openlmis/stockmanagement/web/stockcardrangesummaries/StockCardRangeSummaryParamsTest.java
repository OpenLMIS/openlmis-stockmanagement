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
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_INVALID_PARAMS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.web.stockcardrangesummary.StockCardRangeSummaryParams;
import org.springframework.util.LinkedMultiValueMap;

@SuppressWarnings("PMD.TooManyMethods")
public class StockCardRangeSummaryParamsTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private static final UUID VALUE = randomUUID();
  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";
  private static final String ORDERABLE_ID = "orderableId";
  private static final String TAG = "tag";
  private static final String START_DATE = "startDate";
  private static final String END_DATE = "endDate";

  private LinkedMultiValueMap<String, String> queryMap;

  @Before
  public void setUp() {
    queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM_ID, VALUE.toString());
    queryMap.add(FACILITY_ID, VALUE.toString());
  }

  @Test
  public void shouldGetProgramIdValueFromParameters() {
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertEquals(VALUE, params.getProgramId());
  }

  @Test
  public void shouldGetFacilityIdValueFromParameters() {
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertEquals(VALUE, params.getFacilityId());
  }

  @Test
  public void shouldGetMultipleOrderableIdsFromParameters() {
    UUID id = randomUUID();
    queryMap.add(ORDERABLE_ID, VALUE.toString());
    queryMap.add(ORDERABLE_ID, id.toString());
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertThat(params.getOrderableIds(), hasItems(id, VALUE));
  }

  @Test
  public void shouldAssignNullIfOrderableIdIsAbsentInParameters() {
    StockCardRangeSummaryParams params =
        new StockCardRangeSummaryParams(queryMap);

    assertNull(params.getOrderableIds());
  }

  @Test
  public void shouldGetTagValueFromParameters() {
    queryMap.add(TAG, "tag");
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertEquals("tag", params.getTag());
  }

  @Test
  public void shouldAssignNullIfTagIsAbsentInParameters() {
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertNull(params.getTag());
  }

  @Test
  public void shouldGetStartDateValueFromParameters() {
    queryMap.add(START_DATE, "2017-10-10");
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertEquals(LocalDate.of(2017, 10, 10), params.getStartDate());
  }

  @Test
  public void shouldAssignNullIfStartDateIsAbsentInParameters() {
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertNull(params.getStartDate());
  }

  @Test
  public void shouldGetEndDateValueFromParameters() {
    queryMap.add(END_DATE, "2017-10-10");
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertEquals(LocalDate.of(2017, 10, 10), params.getEndDate());
  }

  @Test
  public void shouldAssignNullIfEndDateIsAbsentInParameters() {
    StockCardRangeSummaryParams params = new StockCardRangeSummaryParams(queryMap);

    assertNull(params.getEndDate());
  }

  @Test
  public void shouldThrowExceptionIfThereIsUnknownParameterInParameters() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_INVALID_PARAMS);

    queryMap.add("some-param", "some-value");
    new StockCardRangeSummaryParams(queryMap);
  }

  @Test
  public void shouldThrowExceptionIfThereIsNoProgramId() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_PROGRAM_ID_MISSING);

    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(FACILITY_ID, "some-value");
    new StockCardRangeSummaryParams(queryMap);
  }

  @Test
  public void shouldThrowExceptionIfThereIsNoFacilityId() {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(ERROR_FACILITY_ID_MISSING);

    LinkedMultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
    queryMap.add(PROGRAM_ID, "some-value");
    new StockCardRangeSummaryParams(queryMap);
  }
}
