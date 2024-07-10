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

package org.openlmis.stockmanagement.web;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_INVALID_PARAMS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_TYPE_INVALID;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.EnumUtils;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.UuidUtil;
import org.springframework.util.MultiValueMap;

public class ValidReasonAssignmentSearchParams {

  static final String PROGRAM = "program";
  static final String FACILITY_TYPE = "facilityType";
  static final String REASON_TYPE = "reasonType";
  static final String REASON = "reason";
  static final String ACTIVE = "active";

  private SearchParams queryParams;

  /**
   * Wraps map of query params into an object.
   */
  public ValidReasonAssignmentSearchParams(MultiValueMap<String, String> queryMap) {
    queryParams = new SearchParams(queryMap);
    validate();
  }

  /**
   * Gets collection of {@link UUID} for "program" key from params.
   */
  public Collection<UUID> getProgramIds() {
    if (!queryParams.containsKey(PROGRAM)) {
      return null;
    }

    Set<UUID> programs = new HashSet<>();
    queryParams.asMultiValueMap().forEach((key, value) -> {
      if (Objects.equals(key, PROGRAM)) {
        value.forEach(id -> {
          if (id != null && !id.isEmpty()) {
            programs.add(UuidUtil.fromString(id).get());
          }
        });
      }
    });

    return programs;
  }

  /**
   * Gets facility type.
   *
   * @return String value of facility type id or null if params doesn't contain this param.
   *         Empty string for null request param value.
   */
  public UUID getFacilityType() {
    if (!queryParams.containsKey(FACILITY_TYPE)) {
      return null;
    }
    Object facilityType = queryParams.getFirst(FACILITY_TYPE);
    return UuidUtil.fromString((String)facilityType).orElse(null);
  }

  /**
   * Gets reason.
   *
   * @return String value of reason id or null if params doesn't contain "reason" param.
   *         Empty string for null request param value.
   */
  public UUID getReason() {
    if (!queryParams.containsKey(REASON)) {
      return null;
    }
    Object reason = queryParams.getFirst(REASON);
    return UuidUtil.fromString((String)reason).orElse(null);
  }

  /**
   * Gets collection of {@link String} for "reasonTypes" key from params.
   */
  public Collection<ReasonType> getReasonType() {
    if (!queryParams.containsKey(REASON_TYPE)) {
      return null;
    }
    Set<ReasonType> reasonTypes = new HashSet<>();
    queryParams.asMultiValueMap().forEach((key, value) -> {
      if (Objects.equals(key, REASON_TYPE)) {
        value.forEach(reasonType -> reasonTypes.add(ReasonType.fromString(reasonType)));
      }
    });

    return reasonTypes;
  }

  /**
   * Checks if query params are valid. Returns false if any provided param is not on supported
   * list.
   */
  public void validate() {
    if (!Collections.unmodifiableList(Arrays.asList(PROGRAM, FACILITY_TYPE, REASON, REASON_TYPE))
        .containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_PARAMS));
    } else {
      queryParams.asMultiValueMap().forEach((key, value) -> {
        if (Objects.equals(key, REASON_TYPE)
            && value.stream()
            .anyMatch(reasonType -> !EnumUtils.isValidEnum(ReasonType.class, reasonType))) {
          throw new ValidationMessageException(new Message(ERROR_REASON_TYPE_INVALID, queryParams));
        }
      });
    }
  }

}
