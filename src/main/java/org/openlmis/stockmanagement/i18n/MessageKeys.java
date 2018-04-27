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
  private static final String PHYSICAL_INVENTORY_ERROR_PREFIX = ERROR_PREFIX + ".physicalInventory";
  public static final String ERROR_USER_NOT_FOUND = ERROR_PREFIX + ".user.notFound";

  //stock card templates
  public static final String ERROR_STOCK_CARD_FIELD_INVALID =
      ERROR_PREFIX + ".field.invalid";

  public static final String ERROR_STOCK_CARD_FIELD_DUPLICATED =
      ERROR_PREFIX + ".field.duplicated";

  public static final String ERROR_PROGRAM_ID_MISSING =
      ERROR_PREFIX + ".program.id.missing";
  public static final String ERROR_FACILITY_ID_MISSING =
      ERROR_PREFIX + ".facility.id.missing";
  public static final String ERROR_UUID_WRONG_FORMAT =
      ERROR_PREFIX + ".uuid.wrongFormat";
  public static final String ERROR_DATE_WRONG_FORMAT =
      ERROR_PREFIX + ".date.wrongFormat";
  public static final String ERROR_FACILITY_TYPE_ID_MISSING =
      ERROR_PREFIX + ".facilityType.id.missing";

  public static final String ERROR_PROGRAM_NOT_FOUND =
      ERROR_PREFIX + ".program.notFound";
  public static final String ERROR_FACILITY_TYPE_NOT_FOUND =
      ERROR_PREFIX + ".facilityType.notFound";

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

  public static final String ERROR_STOCK_EVENT_ORDERABLE_DISABLED_VVM =
      EVENT_ERROR_PREFIX + ".orderable.disabled.vvm";

  //stock events creation: mandatory fields
  public static final String ERROR_EVENT_OCCURRED_DATE_INVALID = EVENT_ERROR_PREFIX
      + ".occurredDate.invalid";

  public static final String ERROR_EVENT_OCCURRED_DATE_IN_FUTURE = EVENT_ERROR_PREFIX
      + ".occurredDate.in.future";

  public static final String ERROR_EVENT_QUANTITIES_INVALID = EVENT_ERROR_PREFIX
      + ".quantities.invalid";

  public static final String ERROR_EVENT_FACILITY_INVALID = EVENT_ERROR_PREFIX
      + ".facilityId.invalid";

  public static final String ERROR_EVENT_PROGRAM_INVALID = EVENT_ERROR_PREFIX
      + ".programId.invalid";

  public static final String ERROR_EVENT_ORDERABLE_INVALID = EVENT_ERROR_PREFIX
      + ".orderableId.invalid";

  public static final String ERROR_EVENT_NO_LINE_ITEMS = EVENT_ERROR_PREFIX
      + ".no.lineItems";

  public static final String ERROR_EVENT_ADJUSTMENT_QUANITITY_INVALID = EVENT_ERROR_PREFIX
      + ".adjustment.quantity.invalid";

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

  //stock events creation: soh
  public static final String ERROR_EVENT_DEBIT_QUANTITY_EXCEED_SOH = EVENT_ERROR_PREFIX
      + ".debit.quantity.exceed.stockOnHand";
  public static final String ERRRO_EVENT_SOH_EXCEEDS_LIMIT = EVENT_ERROR_PREFIX
      + ".stockOnHand.exceed.upperLimit";
  //stock events creation: reason assignment
  //reason.not.in.validList error key is not used for now, because we remove valid reasons
  //check when implementing adjustment's UI
  public static final String ERROR_EVENT_REASON_NOT_IN_VALID_LIST = EVENT_ERROR_PREFIX
      + ".reason.not.in.validList";

  public static final String ERROR_EVENT_REASON_NOT_EXIST = EVENT_ERROR_PREFIX
      + ".reason.not.exist";

  //stock events creation: orderable duplication
  public static final String ERROR_EVENT_ORDERABLE_LOT_DUPLICATION = EVENT_ERROR_PREFIX
      + ".orderable.and.lot.duplication";

  //stock events creation: lot
  public static final String ERROR_EVENT_LOT_NOT_EXIST = EVENT_ERROR_PREFIX + ".lot.not.exist";
  public static final String ERROR_EVENT_LOT_ORDERABLE_NOT_MATCH = EVENT_ERROR_PREFIX
      + ".lot.not.match.orderable";

  //reason configuration
  public static final String ERROR_REASON_ID_EMPTY = ERROR_PREFIX
      + ".reason.id.isEmpty";

  public static final String ERROR_REASON_NOT_FOUND = ERROR_PREFIX
      + ".reason.notFound";

  public static final String ERROR_REASON_TYPE_INVALID = ERROR_PREFIX
      + ".reason.reasonType.invalid";

  public static final String ERROR_REASON_CATEGORY_INVALID = ERROR_PREFIX
      + ".reason.reasonCategory.invalid";

  public static final String ERROR_REASON_ASSIGNMENT_NOT_FOUND = ERROR_PREFIX
      + ".reasonAssignment.notFound";

  //permission error
  public static final String ERROR_NO_FOLLOWING_PERMISSION = ERROR_PREFIX
      + ".authorization.noFollowingPermission";

  public static final String ERROR_FACILITY_TYPE_HOME_FACILITY_TYPE_NOT_MATCH = ERROR_PREFIX
      + ".authorization.facilityTypeAndHomeFacilityType.not.match";

  public static final String ERROR_PROGRAM_NOT_SUPPORTED = ERROR_PREFIX
      + ".authorization.program.not.supported";

  public static final String ERROR_PERMISSION_CHECK_FAILED = ERROR_PREFIX
      + ".authorization.failed";

  //stock card line item reason
  public static final String ERROR_LINE_ITEM_REASON_NAME_MISSING = ERROR_PREFIX
      + ".lineItem.reason.name.missing";

  public static final String ERROR_LINE_ITEM_REASON_TYPE_MISSING = ERROR_PREFIX
      + ".lineItem.reason.type.missing";

  public static final String ERROR_LINE_ITEM_REASON_CATEGORY_MISSING = ERROR_PREFIX
      + ".lineItem.reason.category.missing";

  public static final String ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_MISSING = ERROR_PREFIX
      + ".lineItem.reason.isFreeTextAllowed.missing";

  public static final String ERROR_LINE_ITEM_REASON_NAME_DUPLICATE = ERROR_PREFIX
      + ".lineItem.reason.name.duplicate";

  public static final String ERROR_LINE_ITEM_REASON_ID_NOT_FOUND = ERROR_PREFIX
      + ".lineItem.reason.id.notFound";

  //source destination configuration
  public static final String ERROR_ORGANIZATION_NAME_MISSING = ERROR_PREFIX
      + ".organization.name.missing";

  public static final String ERROR_ORGANIZATION_ID_NOT_FOUND = ERROR_PREFIX
      + ".organization.id.notFound";

  public static final String ERROR_ORGANIZATION_UPDATE_CONTENT_DUPLICATE = ERROR_PREFIX
      + ".organization.update.content.duplicate";

  public static final String ERROR_SOURCE_ASSIGNMENT_NOT_FOUND = ERROR_PREFIX
      + ".source.assignment.notFound";

  public static final String ERROR_DESTINATION_ASSIGNMENT_NOT_FOUND = ERROR_PREFIX
      + ".destination.assignment.notFound";

  public static final String ERROR_SOURCE_NOT_FOUND = ERROR_PREFIX
      + ".source.notFound";

  public static final String ERROR_DESTINATION_NOT_FOUND = ERROR_PREFIX
      + ".destination.notFound";

  public static final String ERROR_SOURCE_DESTINATION_ASSIGNMENT_ID_MISSING = ERROR_PREFIX
      + ".source.destination.assignment.id.missing";

  //physical inventory
  public static final String ERROR_PHYSICAL_INVENTORY_LINE_ITEMS_MISSING =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".lineItems.missing";

  public static final String ERROR_PHYSICAL_INVENTORY_ORDERABLE_MISSING =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".orderable.missing";

  public static final String ERROR_PHYSICAL_INVENTORY_NOT_INCLUDE_ACTIVE_STOCK_CARD =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".lineItems.not.include.active.stockCard";

  public static final String ERROR_PHYSICAL_INVENTORY_ORDERABLE_DISABLED_VVM =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".orderable.disabled.vvm";

  public static final String ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_VALID =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".lineItems.stockAdjustments.reason.notValid";

  public static final String ERROR_PHYSICAL_INVENTORY_DISCREPANCY_REASON_NOT_PROVIDED =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".lineItems.stockAdjustments.reason.notProvided";

  public static final String ERROR_PHYSICAL_INVENTORY_DISCREPANCY_QUANTITY_NOT_PROVIDED =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".lineItems.stockAdjustments.quantity.notProvided";

  public static final String ERROR_PHYSICAL_INVENTORY_NOT_FOUND =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".notFound";

  public static final String ERROR_PHYSICAL_INVENTORY_FORMAT_NOT_ALLOWED =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".format.notAllowed";

  public static final String ERROR_PHYSICAL_INVENTORY_ID_MISMATCH =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".id.mismatch";

  public static final String ERROR_PHYSICAL_INVENTORY_IS_SUBMITTED =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".isSubmitted";

  public static final String ERROR_PHYSICAL_INVENTORY_DRAFT_EXISTS =
      PHYSICAL_INVENTORY_ERROR_PREFIX + ".draft.exists";

  //report
  public static final String ERROR_GENERATE_REPORT_FAILED = ERROR_PREFIX + ".generateReport.failed";

  public static final String ERROR_REPORT_ID_NOT_FOUND = ERROR_PREFIX + ".report.id.notFound";

  public static final String ERROR_JASPER_FILE_CREATION = ERROR_PREFIX + ".jasper.fileCreation";
  public static final String ERROR_CLASS_NOT_FOUND = ERROR_PREFIX + ".classNotFound";
  public static final String ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME = ERROR_PREFIX
      + ".reporting.template.notFound.with.name";
  public static final String ERROR_REPORTING_CREATION = ERROR_PREFIX + ".reporting.creation";
  public static final String ERROR_REPORTING_FILE_MISSING = ERROR_PREFIX
      + ".reporting.file.missing";
  public static final String ERROR_REPORTING_FILE_INVALID = ERROR_PREFIX
      + ".reporting.file.invalid";
  public static final String ERROR_REPORTING_FILE_INCORRECT_TYPE = ERROR_PREFIX
      + ".reporting.file.incorrectType";
  public static final String ERROR_REPORTING_FILE_EMPTY = ERROR_PREFIX + ".reporting.file.empty";

  //notifications
  public static final String EMAIL_ACTION_REQUIRED_SUBJECT =
      SERVICE_PREFIX + ".email.stockout.subject";
  public static final String EMAIL_ACTION_REQUIRED_CONTENT =
      SERVICE_PREFIX + ".email.stockout.content";

  //server errors
  public static final String SERVER_ERROR_SHALLOW_COPY
      = SERVICE_PREFIX + ".error.shallowCopy";
  public static final String ERROR_IO = ERROR_PREFIX + ".io";

  public static final String ERROR_ENCODING = ERROR_PREFIX + ".encoding.notSupported";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
