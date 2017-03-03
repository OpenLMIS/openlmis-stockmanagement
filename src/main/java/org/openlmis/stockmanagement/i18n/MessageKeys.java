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

  //stock events creation: approved products
  public static final String ERROR_ORDERABLE_NOT_IN_APPROVED_LIST =
      EVENT_ERROR_PREFIX + ".orderable.not.in.approvedList";

  //stock events creation: source and destination assignment
  public static final String ERROR_SOURCE_DESTINATION_BOTH_PRESENT =
      EVENT_ERROR_PREFIX + ".sourceAndDestination.bothPresent";

  public static final String ERROR_SOURCE_NOT_IN_VALID_LIST =
      EVENT_ERROR_PREFIX + ".source.not.in.validList";

  public static final String ERROR_DESTINATION_NOT_IN_VALID_LIST =
      EVENT_ERROR_PREFIX + ".destination.not.in.validList";

  //stock events creation: free texts
  public static final String ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT =
      EVENT_ERROR_PREFIX + ".sourceAndDestinationFreeText.bothPresent";

  public static final String ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED =
      EVENT_ERROR_PREFIX + ".sourceFreeText.notAllowed";

  public static final String ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED =
      EVENT_ERROR_PREFIX + ".destinationFreeText.notAllowed";

  public static final String ERROR_REASON_FREE_TEXT_NOT_ALLOWED =
      EVENT_ERROR_PREFIX + ".reasonFreeText.notAllowed";

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
      + ".orderableId.invalid";

  //stock events creation: adjustment reason
  public static final String ERROR_EVENT_ADJUSTMENT_REASON_TYPE_INVALID = EVENT_ERROR_PREFIX
      + ".adjustment.reason.type.invalid";

  public static final String ERROR_EVENT_ADJUSTMENT_REASON_CATEGORY_INVALID = EVENT_ERROR_PREFIX
      + ".adjustment.reason.category.invalid";

  //stock events creation: receive issue reason
  public static final String ERROR_EVENT_RECEIVE_REASON_TYPE_INVALID = EVENT_ERROR_PREFIX
      + ".receive.reasonType.invalid";

  public static final String ERROR_EVENT_ISSUE_REASON_TYPE_INVALID = EVENT_ERROR_PREFIX
      + ".issue.reasonType.invalid";

  public static final String ERROR_EVENT_RECEIVE_REASON_CATEGORY_INVALID = EVENT_ERROR_PREFIX
      + ".receive.reasonCategory.invalid";

  public static final String ERROR_EVENT_ISSUE_REASON_CATEGORY_INVALID = EVENT_ERROR_PREFIX
      + ".issue.reasonCategory.invalid";

  //stock events creation: debit quantity
  public static final String ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH = EVENT_ERROR_PREFIX
      + ".debit.quantity.exceed.stockOnHand";

  //stock events creation: reason assignment
  public static final String ERROR_EVENT_REASON_NOT_IN_VALID_LIST = EVENT_ERROR_PREFIX
      + ".reason.not.in.validList";

  //reason configuration
  public static final String ERROR_REASON_ID_EMPTY = ERROR_PREFIX
      + ".reason.id.isEmpty";

  public static final String ERROR_REASON_NOT_FOUND = ERROR_PREFIX
      + ".reason.notFound";

  //permission error
  public static final String ERROR_NO_FOLLOWING_PERMISSION = ERROR_PREFIX
      + ".authorization.noFollowingPermission";

  public static final String ERROR_FACILITY_TYPE_HOME_FACILITY_TYPE_NOT_MATCH = ERROR_PREFIX
      + ".authorization.facilityTypeAndHomeFacilityType.not.match";

  public static final String ERROR_PROGRAM_NOT_SUPPORTED = ERROR_PREFIX
      + ".authorization.program.not.supported";

  //stock card line item reason
  public static final String ERROR_LINE_ITEM_REASON_NAME_MISSING = ERROR_PREFIX
      + ".lineItem.reason.name.missing";

  public static final String ERROR_LINE_ITEM_REASON_TYPE_MISSING = ERROR_PREFIX
      + ".lineItem.reason.type.missing";

  public static final String ERROR_LINE_ITEM_REASON_CATEGORY_MISSING = ERROR_PREFIX
      + ".lineItem.reason.category.missing";

  public static final String ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_MISSING = ERROR_PREFIX
      + ".lineItem.reason.isFreeTextAllowed.missing";

  public static final String ERROR_LINE_ITEM_REASON_ID_NOT_FOUND = ERROR_PREFIX
      + ".lineItem.reason.id.notFound";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
