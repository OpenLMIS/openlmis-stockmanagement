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

import static java.util.Arrays.asList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_INVALID_PARAMS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROVIDED_FACILITY_ID_WITHOUT_PROGRAM_ID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROVIDED_PROGRAM_ID_WITHOUT_FACILITY_ID;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.UuidUtil;
import org.springframework.util.MultiValueMap;

public class ValidSourceDestinationSearchParams {

  public static final String PROGRAM_ID = "programId";
  public static final String FACILITY_ID = "facilityId";
  public static final String GEOGRAPHIC_ZONE_ID = "geographicZoneId";

  private SearchParams queryParams;

  /**
   * Wraps map of query params into an object.
   */
  public ValidSourceDestinationSearchParams(MultiValueMap<String, String> queryMap) {
    queryParams = new SearchParams(queryMap);
    validate();
  }

  /**
   * Gets program id.
   *
   * @return UUID value of program id or null if params doesn't contain this param.
   */
  public UUID getProgramId() {
    if (!queryParams.containsKey(PROGRAM_ID)) {
      return null;
    }
    String program = queryParams.getFirst(PROGRAM_ID);
    return UuidUtil.fromString(program).orElse(null);
  }

  /**
   * Gets all program IDs.
   *
   * @return list of program UUID or empty list (if params doesn't contain this param)
   */
  public List<UUID> getAllProgramIds() {
    if (!queryParams.containsKey(PROGRAM_ID)) {
      return Collections.emptyList();
    }

    return queryParams.get(PROGRAM_ID).stream()
        .map(uuid -> UuidUtil.fromString(uuid).orElse(null))
        .collect(Collectors.toList());
  }

  /**
   * Gets facility id.
   *
   * @return UUID value of facility id or null if params doesn't contain this param.
   */
  public UUID getFacilityId() {
    if (!queryParams.containsKey(FACILITY_ID)) {
      return null;
    }
    String facilityType = queryParams.getFirst(FACILITY_ID);
    return UuidUtil.fromString(facilityType).orElse(null);
  }

  /**
   * Gets geographic zone id.
   *
   * @return UUID value of geographic zone id or null if params doesn't contain this param.
   */
  public UUID getGeographicZone() {
    if (!queryParams.containsKey(GEOGRAPHIC_ZONE_ID)) {
      return null;
    }
    String geographicZone = queryParams.getFirst(GEOGRAPHIC_ZONE_ID);
    return UuidUtil.fromString(geographicZone).orElse(null);
  }

  private void validate() {
    if (!Collections.unmodifiableList(asList(PROGRAM_ID, FACILITY_ID, GEOGRAPHIC_ZONE_ID))
        .containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_PARAMS));
    }
    if (queryParams.keySet().contains(PROGRAM_ID) && !queryParams.keySet().contains(FACILITY_ID)) {
      throw new ValidationMessageException(
          new Message(ERROR_PROVIDED_PROGRAM_ID_WITHOUT_FACILITY_ID));
    }
    if (queryParams.keySet().contains(FACILITY_ID) && !queryParams.keySet().contains(PROGRAM_ID)) {
      throw new ValidationMessageException(
          new Message(ERROR_PROVIDED_FACILITY_ID_WITHOUT_PROGRAM_ID));
    }
  }
}
