package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.UuidUtil;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_INVALID_PARAMS;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROVIDED_FACILITY_ID_WITHOUT_PROGRAM_ID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROVIDED_PROGRAM_ID_WITHOUT_FACILITY_ID;

public class ValidSourceDestinationSearchParams {

  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";

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

  private void validate() {
    if (!Collections.unmodifiableList(asList(PROGRAM_ID, FACILITY_ID))
        .containsAll(queryParams.keySet())) {
      throw new ValidationMessageException(new Message(ERROR_INVALID_PARAMS));
    }
    if (queryParams.keySet().contains(PROGRAM_ID) && !queryParams.keySet().contains(FACILITY_ID)) {
      throw new ValidationMessageException(new Message(ERROR_PROVIDED_PROGRAM_ID_WITHOUT_FACILITY_ID));
    }
    if (queryParams.keySet().contains(FACILITY_ID) && !queryParams.keySet().contains(PROGRAM_ID)) {
      throw new ValidationMessageException(new Message(ERROR_PROVIDED_FACILITY_ID_WITHOUT_PROGRAM_ID));
    }
  }
}
