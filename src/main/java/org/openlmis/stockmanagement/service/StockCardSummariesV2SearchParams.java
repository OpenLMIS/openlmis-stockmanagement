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

import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    if (!isEmpty(parameters)) {
      String programId = parameters.getFirst(PROGRAM_ID);
      if (null != programId) {
        this.programId = UUID.fromString(programId);
      }

      String facilityId  = parameters.getFirst(FACILITY_ID);
      if (null != facilityId) {
        this.facilityId = UUID.fromString(facilityId);
      }

      String asOfDate  = parameters.getFirst(AS_OF_DATE);
      if (null != asOfDate) {
        this.asOfDate = LocalDate.parse(asOfDate, DateTimeFormatter.ISO_DATE);
      }

      List<String> orderableIds = parameters.get(ORDERABLE_ID);
      if (null != orderableIds) {
        this.orderableIds = parameters.get(ORDERABLE_ID).stream()
            .map(UUID::fromString)
            .collect(Collectors.toList());
      } else {
        this.orderableIds = new ArrayList<>();
      }

      page = parameters.getFirst(PAGE);
      size = parameters.getFirst(SIZE);
    }

    pageable = new PageRequest(page != null ? new Integer(page) : 0,
        size != null ? new Integer(size) : Integer.MAX_VALUE);
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
}
