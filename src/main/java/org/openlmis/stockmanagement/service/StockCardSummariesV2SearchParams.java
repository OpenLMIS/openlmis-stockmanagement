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

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DATE_WRONG_FORMAT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PAGINATION_WRONG_PARAMETERS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_UUID_WRONG_FORMAT;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections.MapUtils;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class StockCardSummariesV2SearchParams {

  static final String PROGRAM_ID = "programId";
  static final String FACILITY_ID = "facilityId";
  static final String AS_OF_DATE = "asOfDate";
  static final String ORDERABLE_ID = "orderableId";
  static final String PAGE = "page";
  static final String SIZE = "size";

  private UUID programId;
  private UUID facilityId;
  private List<UUID> orderableIds;
  private LocalDate asOfDate;
  private Pageable pageable;

  /**
   * Creates stock card summaries search params from multi value map.
   */
  public StockCardSummariesV2SearchParams(MultiValueMap<String, String> parameters) {
    String page = null;
    String size = null;

    if (!MapUtils.isEmpty(parameters)) {
      this.programId = getId(PROGRAM_ID, parameters);
      this.facilityId = getId(FACILITY_ID, parameters);
      this.asOfDate = getDate(AS_OF_DATE, parameters);
      this.orderableIds = getIds(ORDERABLE_ID, parameters);

      page = parameters.getFirst(PAGE);
      size = parameters.getFirst(SIZE);
    }

    try {
      pageable = new PageRequest(page != null ? new Integer(page) : 0,
          size != null ? new Integer(size) : Integer.MAX_VALUE);
    } catch (IllegalArgumentException ex) {
      throw new ValidationMessageException(ex,
          new Message(ERROR_PAGINATION_WRONG_PARAMETERS, page, size));
    }
  }

  /**
   * Validates if this search params contains a valid parameters.
   */
  public void validate() {
    if (null == facilityId) {
      throw new ValidationMessageException(ERROR_FACILITY_ID_MISSING);
    }

    if (null == programId) {
      throw new ValidationMessageException(ERROR_PROGRAM_ID_MISSING);
    }
  }

  private List<UUID> getIds(String fieldName, MultiValueMap<String, String> parameters) {
    List<String> ids = parameters.get(fieldName);
    if (!isEmpty(ids)) {
      return ids.stream()
          .map(id -> formatId(id, fieldName))
          .collect(toList());
    }
    return new ArrayList<>();
  }

  private UUID getId(String fieldName, MultiValueMap<String, String> parameters) {
    String id = parameters.getFirst(fieldName);
    return formatId(id, fieldName);
  }

  private UUID formatId(String id, String fieldName) {
    if (null != id) {
      try {
        return UUID.fromString(id);
      } catch (IllegalArgumentException ex) {
        throw new ValidationMessageException(ex,
            new Message(ERROR_UUID_WRONG_FORMAT, id, fieldName));
      }
    }
    return null;
  }

  private LocalDate getDate(String fieldName, MultiValueMap<String, String> parameters) {
    String date = parameters.getFirst(fieldName);
    if (null != date) {
      try {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
      } catch (DateTimeParseException ex) {
        throw new ValidationMessageException(ex,
            new Message(ERROR_DATE_WRONG_FORMAT, date, fieldName));
      }
    }
    return null;
  }
}
