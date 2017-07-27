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

import static org.openlmis.stockmanagement.util.RequestHelper.createEntityWithAuthHeader;

import org.openlmis.stockmanagement.service.AuthService;
import org.openlmis.stockmanagement.util.DynamicPageTypeReference;
import org.openlmis.stockmanagement.util.PageImplRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseCommunicationService<T> {
  protected static final String ACCESS_TOKEN = "access_token";

  protected RestOperations restTemplate = new RestTemplate();

  protected abstract String getServiceUrl();

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  @Autowired
  private AuthService authService;

  public BaseCommunicationService() {
    this.restTemplate = new RestTemplate();
  }

  protected String obtainAccessToken() {
    return authService.obtainAccessToken();
  }

  protected URI buildUri(String url) {
    return buildUri(url, null);
  }

  protected URI buildUri(String url, Map<String, ?> params) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    if (params != null) {
      params.forEach(builder::queryParam);
    }

    return builder.build(true).toUri();
  }

  protected Page<T> getPage(String resourceUrl, Map<String, Object> parameters) {
    return getPage(resourceUrl, parameters, null, HttpMethod.GET, getResultClass());
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with POST request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param payload     body to include with the outgoing request.
   * @return Page of reference data T objects.
   */
  protected Page<T> getPage(String resourceUrl, Map<String, Object> parameters, Object payload) {
    return getPage(resourceUrl, parameters, payload, HttpMethod.POST, getResultClass());
  }

  protected <P> Page<P> getPage(String resourceUrl, Map<String, Object> parameters, Object payload,
                                HttpMethod method, Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    Map<String, Object> params = new HashMap<>();
    params.putAll(parameters);

    try {
      ResponseEntity<PageImplRepresentation<P>> response = restTemplate.exchange(
          buildUri(url, params),
          method,
          createEntityWithAuthHeader(payload, obtainAccessToken()),
          new DynamicPageTypeReference<>(type)
      );
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected DataRetrievalException buildDataRetrievalException(
      HttpStatusCodeException ex) {
    return new DataRetrievalException(
        getResultClass().getSimpleName(), ex.getStatusCode(), ex.getResponseBodyAsString()
    );
  }
}
