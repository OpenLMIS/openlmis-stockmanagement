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

package org.openlmis.stockmanagement.web.stockcardrangesummary;

import static java.util.Arrays.asList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_INVALID_PARAMS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.web.SearchParams;
import org.springframework.util.MultiValueMap;

public class StockCardRangeSummaryParams {

  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";
  private static final String ORDERABLE_ID = "orderableId";
  private static final String TAG = "tag";
  private static final String START_DATE = "startDate";
  private static final String END_DATE = "endDate";

  private static final List<String> ALL_PARAMETERS =
      asList(PROGRAM_ID, FACILITY_ID, ORDERABLE_ID, TAG, START_DATE, END_DATE);

  private SearchParams queryParams;

  /**
   * Wraps map of query params into an object.
   */
  public StockCardRangeSummaryParams(MultiValueMap<String, String> queryMap) {
    queryParams = new SearchParams(queryMap);
    validate();
  }

  /**
   * Gets program id.
   * If param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return String value of program id or null if params doesn't contain "programId" param.
   */
  public UUID getProgramId() {
    if (!queryParams.containsKey(PROGRAM_ID)) {
      return null;
    }
    return queryParams.getUuid(queryParams.getFirst(PROGRAM_ID));
  }

  /**
   * Gets facility id.
   * If param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return UUID value of facility id or null if params doesn't contain "facilityId" param.
   */
  public UUID getFacilityId() {
    if (!queryParams.containsKey(FACILITY_ID)) {
      return null;
    }
    return queryParams.getUuid(queryParams.getFirst(FACILITY_ID));
  }

  /**
   * Gets orderable ids.
   * If any param value has incorrect format {@link ValidationMessageException} will be thrown.
   *
   * @return Collections of orderable ids or null if params doesn't contain "orderableId" param.
   */
  public Set<UUID> getOrderableIds() {
    if (!queryParams.containsKey(ORDERABLE_ID)) {
      return null;
    }
    return queryParams.get(ORDERABLE_ID).stream()
        .map(orderableId -> queryParams.getUuid(orderableId))
        .collect(Collectors.toSet());
  }

  /**
   * Gets {@link String} for "tag" key from params.
   *
   * @return String value of tag or null if params doesn't contain "tag" param.
   */
  public String getTag() {
    if (!queryParams.containsKey(TAG)) {
      return null;
    }
    return queryParams.getFirst(TAG);
  }

  /**
   * Gets {@link LocalDate} for "startDate" key from params.
   *
   * @return value of start date or null if params doesn't contain "startDate" param.
   */
  public LocalDate getStartDate() {
    if (!queryParams.containsKey(START_DATE)) {
      return null;
    }
    return LocalDate.parse(queryParams.getFirst(START_DATE));
  }

  /**
   * Gets {@link LocalDate} for "endDate" key from params.
   *
   * @return value of end date or null if params doesn't contain "endDate" param.
   */
  public LocalDate getEndDate() {
    if (!queryParams.containsKey(END_DATE)) {
      return null;
    }
    return LocalDate.parse(queryParams.getFirst(END_DATE));
  }

  /**
   * Checks if query params are valid. Returns false if any provided param is not on supported
   * list.
   */
  public void validate() {
    if (!Collections.unmodifiableList(ALL_PARAMETERS)
        .containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_PARAMS));
    }
    if (!queryParams.containsKey(PROGRAM_ID)) {
      throw new ValidationMessageException(new Message(ERROR_PROGRAM_ID_MISSING));
    }
    if (!queryParams.containsKey(FACILITY_ID)) {
      throw new ValidationMessageException(new Message(ERROR_FACILITY_ID_MISSING));
    }
  }
}
