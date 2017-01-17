package org.openlmis.stockmanagement.errorhandling;

import org.openlmis.stockmanagement.exception.AuthenticationException;
import org.openlmis.stockmanagement.exception.MissingPermissionException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.ReferenceDataRetrievalException;
import org.openlmis.stockmanagement.util.ErrorResponse;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base classes for controller advices dealing with error handling.
 */
@ControllerAdvice
public class GlobalErrorHandling extends AbstractErrorHandling {

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ResponseBody
  public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
    return logErrorAndRespond("Could not authenticate user", ex);
  }

  @ExceptionHandler(MissingPermissionException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  public ErrorResponse handlePermissionException(MissingPermissionException ex) {
    return logErrorAndRespond("User is lacking permission to access the resource", ex);
  }

  @ExceptionHandler(ReferenceDataRetrievalException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse handleRefDataException(ReferenceDataRetrievalException ex) {
    return logErrorAndRespond("Error fetching from reference data", ex);
  }

  /**
   * Handles Message exceptions and returns status 400 Bad Request.
   *
   * @param ex the ValidationMessageException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(ValidationMessageException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Message.LocalizedMessage handleMessageException(ValidationMessageException ex) {
    return getLocalizedMessage(ex);
  }
}
