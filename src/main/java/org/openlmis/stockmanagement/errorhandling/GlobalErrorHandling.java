package org.openlmis.stockmanagement.errorhandling;

import org.openlmis.stockmanagement.exception.AuthenticationException;
import org.openlmis.stockmanagement.exception.FieldsNotAvailableException;
import org.openlmis.stockmanagement.exception.MissingPermissionException;
import org.openlmis.stockmanagement.service.referencedata.ReferenceDataNotFoundException;
import org.openlmis.stockmanagement.service.referencedata.ReferenceDataRetrievalException;
import org.openlmis.stockmanagement.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base classes for controller advices dealing with error handling.
 */
@ControllerAdvice
public class GlobalErrorHandling {

  private final Logger logger = LoggerFactory.getLogger(getClass());

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

  @ExceptionHandler(ReferenceDataNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorResponse handleRefDataException(ReferenceDataNotFoundException ex) {
    return logErrorAndRespond("Error fetching from reference data", ex);
  }

  @ExceptionHandler(FieldsNotAvailableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorResponse handleUnavailableFieldsException(FieldsNotAvailableException ex) {
    return logErrorAndRespond("Not an available field", ex);
  }

  /**
   * Logs an error message and returns an error response.
   *
   * @param message the error message
   * @param ex      the exception to log.
   *                Message from the exception is used as the error description.
   * @return the error response that should be sent to the client
   */
  protected ErrorResponse logErrorAndRespond(String message, Exception ex) {
    logger.error(message, ex);
    return new ErrorResponse(message, ex.getMessage());
  }

}
