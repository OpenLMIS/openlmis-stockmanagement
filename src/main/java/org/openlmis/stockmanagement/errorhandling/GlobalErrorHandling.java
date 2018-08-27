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

package org.openlmis.stockmanagement.errorhandling;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TAGS_INVALID;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.openlmis.stockmanagement.exception.AuthenticationException;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.DataRetrievalException;
import org.openlmis.stockmanagement.util.ErrorResponse;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base classes for controller advices dealing with error handling.
 */
@ControllerAdvice
public class GlobalErrorHandling extends AbstractErrorHandling {
  private static final Map<String, String> SQL_STATES = new HashMap<>();

  static {
    // https://www.postgresql.org/docs/9.6/static/errcodes-appendix.html
    SQL_STATES.put("22001", ERROR_LINE_ITEM_REASON_TAGS_INVALID);
  }

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ResponseBody
  public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
    return logErrorAndRespond("Could not authenticate user", ex);
  }

  @ExceptionHandler(PermissionMessageException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  public Message.LocalizedMessage handlePermissionException(PermissionMessageException ex) {
    return getLocalizedMessage(ex);
  }

  @ExceptionHandler(DataRetrievalException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse handleRefDataException(DataRetrievalException ex) {
    return logErrorAndRespond("Error fetching from reference data", ex);
  }

  /**
   * Handles Message exceptions and returns status 500.
   *
   * @param ex the JasperReportViewException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(JasperReportViewException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public Message.LocalizedMessage handleJasperReportException(JasperReportViewException ex) {
    logger.error("Error generating jasper report failed", ex);

    return getLocalizedMessage(ex);
  }

  /**
   * Handles Message exceptions and returns status 404 NOT_FOUND.
   *
   * @param ex the ResourceNotFoundException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public Message.LocalizedMessage handleResourceNotFoundException(ResourceNotFoundException ex) {
    return getLocalizedMessage(ex);
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

  /**
   * Handles Jpa System Exception.
   * @param ex the Jpa System Exception
   * @return the user-oriented error message.
   */
  @ExceptionHandler(JpaSystemException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Message.LocalizedMessage handleJpaSystemException(JpaSystemException ex) {
    logger.info(ex.getMessage(), ex);

    if (ex.getCause() instanceof PersistenceException) {
      PersistenceException persistence = (PersistenceException) ex.getCause();

      if (persistence.getCause() instanceof SQLException) {
        SQLException sql = (SQLException) persistence.getCause();
        String message = SQL_STATES.get(sql.getSQLState());

        if (null != message) {
          return getLocalizedMessage(message);
        }
      }
    }

    return getLocalizedMessage(ex.getMessage());
  }
}
