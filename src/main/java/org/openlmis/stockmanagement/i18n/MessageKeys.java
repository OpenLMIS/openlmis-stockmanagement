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

package org.openlmis.stockmanagement.i18n;

public abstract class MessageKeys {

  private static final String SERVICE_PREFIX = "stockmanagement";
  private static final String ERROR_PREFIX = SERVICE_PREFIX + ".error";
  private static final String EVENT_ERROR_PREFIX = ERROR_PREFIX + ".event";

  //stock card templates
  public static final String ERROR_STOCK_CARD_FIELD_INVALID =
      ERROR_PREFIX + ".field.invalid";

  public static final String ERROR_PROGRAM_NOT_FOUND =
      ERROR_PREFIX + ".program.notFound";
  public static final String ERROR_FACILITY_TYPE_NOT_FOUND =
      ERROR_PREFIX + ".facilityType.notFound";

  public static final String ERROR_STOCK_EVENT_REASON_NOT_MATCH =
      ERROR_PREFIX + ".reason.notMatch";

  //stock events creation: source and destination
  public static final String ERROR_ORDERABLE_NOT_FOUND =
      EVENT_ERROR_PREFIX + ".orderable.notFound";

  public static final String ERROR_SOURCE_DESTINATION_BOTH_PRESENT =
      EVENT_ERROR_PREFIX + ".sourceAndDestination.bothPresent";

  public static final String ERROR_SOURCE_NOT_VALID =
      EVENT_ERROR_PREFIX + ".source.invalid";

  public static final String ERROR_DESTINATION_NOT_VALID =
      EVENT_ERROR_PREFIX + ".destination.invalid";

  //stock events creation: free texts
  public static final String ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED =
      EVENT_ERROR_PREFIX + ".sourceFreeText.notAllowed";

  //stock events creation: mandatory fields
  public static final String ERROR_EVENT_OCCURRED_DATE_INVALID = EVENT_ERROR_PREFIX
      + ".occurredDate.invalid";

  public static final String ERROR_EVENT_QUANTITY_INVALID = EVENT_ERROR_PREFIX
      + ".quantity.invalid";

  public static final String ERROR_EVENT_FACILITY_INVALID = EVENT_ERROR_PREFIX
      + ".facilityId.invalid";

  public static final String ERROR_EVENT_PROGRAM_INVALID = EVENT_ERROR_PREFIX
      + ".programId.invalid";

  public static final String ERROR_EVENT_ORDERABLE_INVALID = EVENT_ERROR_PREFIX
      + ".ordeableId.invalid";

  //stock events creation: adjustment
  public static final String ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID = EVENT_ERROR_PREFIX
      + ".adjustment.reason.type.invalid";

  public static final String ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID = EVENT_ERROR_PREFIX
      + ".adjustment.reason.category.invalid";

  //stock events creation: receive and issue
  public static final String ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID = EVENT_ERROR_PREFIX
          + ".receive.reasonType.invalid";

  public static final String ERROR_EVENT_ISSUE_REASON_TYPE_INVALID = EVENT_ERROR_PREFIX
          + ".issue.reasonType.invalid";

  public static final String ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID = EVENT_ERROR_PREFIX
          + ".receive.reasonCategory.invalid";

  public static final String ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID = EVENT_ERROR_PREFIX
          + ".issue.reasonCategory.invalid";

  //permission error
  public static final String ERROR_NO_FOLLOWING_PERMISSION = ERROR_PREFIX
      + ".authorization.noFollowingPermission";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
