package org.openlmis.stockmanagement.service.referencedata;

import org.springframework.http.HttpStatus;

public class ReferenceDataNotFoundException extends RuntimeException {

  /**
   * Constructs the exception.
   *
   * @param resource the resource that we were trying to retrieve
   * @param status   the http status that was returned
   * @param response the response from referencedata service
   */
  public ReferenceDataNotFoundException(String resource,
                                        HttpStatus status, String response) {
    super(String.format("Unable to retrieve %s. Error code: %d, response message: %s",
            resource, status.value(), response));
  }
}
