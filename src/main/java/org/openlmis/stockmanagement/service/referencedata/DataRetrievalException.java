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

package org.openlmis.stockmanagement.service.referencedata;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Signals we were unable to retrieve reference data
 * due to a communication error.
 */
public class DataRetrievalException extends RuntimeException {

  @Getter
  private final String resource;

  @Getter
  private final HttpStatus status;

  @Getter
  private final String response;

  /**
   * Constructs the exception.
   *
   * @param resource the resource that we were trying to retrieve
   * @param ex       rest client exception, may contain response from server with code and body
   */
  public DataRetrievalException(String resource, RestClientException ex) {
    this(resource, ex instanceof RestClientResponseException
            ? HttpStatus.resolve(((RestClientResponseException) ex).getRawStatusCode())
            : null,
        ex instanceof RestClientResponseException
            ? ((RestClientResponseException) ex).getResponseBodyAsString()
            : ex.getLocalizedMessage());
  }

  /**
   * Constructs the exception.
   *
   * @param resource the resource that we were trying to retrieve
   * @param status   the http status that was returned
   * @param response the response from referencedata service
   */
  public DataRetrievalException(String resource, HttpStatus status, String response) {
    super(String.format("Unable to retrieve %s. Error code: %d, response message: %s",
        resource, status != null ? status.value() : 0, response));
    this.resource = resource;
    this.status = status;
    this.response = response;
  }
}
