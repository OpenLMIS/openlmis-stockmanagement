package org.openlmis.stockmanagement.exception;

/**
 * Signals user being unauthorized in external api.
 */
public class AuthenticationException extends BaseMessageException {
  public AuthenticationException(String message) {
    super(message);
  }
}
