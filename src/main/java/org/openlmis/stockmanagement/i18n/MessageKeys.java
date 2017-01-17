package org.openlmis.stockmanagement.i18n;

public abstract class MessageKeys {
  private static final String SERVICE_PREFIX = "stockmanagement";
  private static final String ERROR_PREFIX = SERVICE_PREFIX + ".error";

  public static final String ERROR_STOCK_CARD_FIELD_INVALID = ERROR_PREFIX + ".field.invalid";

  public static final String ERROR_PROGRAM_NOT_FOUND = ERROR_PREFIX + ".program.notFound";
  public static final String ERROR_FACILITY_TYPE_NOT_FOUND =
          ERROR_PREFIX + ".facilityType.notFound";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
