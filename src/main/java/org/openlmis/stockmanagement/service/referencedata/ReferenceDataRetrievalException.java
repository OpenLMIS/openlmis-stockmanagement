package org.openlmis.stockmanagement.service.referencedata;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Signals we were unable to retrieve reference data
 * due to a communication error.
 */
public class ReferenceDataRetrievalException extends RuntimeException {

  @Getter
  private String resource;

  @Getter
  private HttpStatus status;

  @Getter
  private String response;

  /**
   * Constructs the exception.
   * @param resource the resource that we were trying to retrieve
   * @param status the http status that was returned
   * @param response the response from referencedata service
   */
  public ReferenceDataRetrievalException(String resource,
                                         HttpStatus status, String response) {
    super(String.format("Unable to retrieve %s. Error code: %d, response message: %s",
            resource, status.value(), response));
    this.resource = resource;
    this.status = status;
    this.response = response;
  }
}
