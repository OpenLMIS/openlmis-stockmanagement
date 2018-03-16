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

package org.openlmis.stockmanagement.service;

import static org.openlmis.stockmanagement.util.RequestHelper.createUri;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.openlmis.stockmanagement.dto.referencedata.ResultDto;
import org.openlmis.stockmanagement.service.referencedata.DataRetrievalException;
import org.openlmis.stockmanagement.util.DynamicPageTypeReference;
import org.openlmis.stockmanagement.util.DynamicParametrizedTypeReference;
import org.openlmis.stockmanagement.util.PageImplRepresentation;
import org.openlmis.stockmanagement.util.RequestHelper;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseCommunicationService<T> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private AuthService authService;

  private RestOperations restTemplate = new RestTemplate();

  protected abstract String getServiceUrl();

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  /**
   * Return one object from service.
   *
   * @param id UUID of requesting object.
   * @return Requesting reference data object.
   */
  public T findOne(UUID id) {
    return findOne(id.toString(), RequestParameters.init());
  }

  /**
   * Return one object from service.
   *
   * @param parameters Map of query parameters.
   * @return Requesting reference data object.
   */
  public T findOne(RequestParameters parameters) {
    return findOne(null, parameters, getResultClass());
  }

  /**
   * Return one object from service.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return one reference data T objects.
   */
  public T findOne(String resourceUrl, RequestParameters parameters) {
    return findOne(resourceUrl, parameters, getResultClass());
  }

  /**
   * Return one object from service.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param type        set to what type a response should be converted.
   * @return one reference data T objects.
   */
  public T findOne(String resourceUrl, RequestParameters parameters, Class<T> type) {
    String url = getServiceUrl() + getUrl() + StringUtils.defaultIfBlank(resourceUrl, "");

    RequestParameters params = RequestParameters
        .init()
        .setAll(parameters);

    try {
      ResponseEntity<T> responseEntity = restTemplate.exchange(
          createUri(url, params), HttpMethod.GET, createEntity(),
          type);
      return responseEntity.getBody();
    } catch (HttpStatusCodeException ex) {
      // rest template will handle 404 as an exception, instead of returning null
      if (HttpStatus.NOT_FOUND == ex.getStatusCode()) {
        logger.warn(
            "{} matching params does not exist. Params: {}",
            getResultClass().getSimpleName(), parameters
        );

        return null;
      } else {
        throw buildDataRetrievalException(ex);
      }
    }
  }

  /**
   * Return all reference data T objects.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return all reference data T objects.
   */
  protected Collection<T> findAll(String resourceUrl, Map<String, Object> parameters) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    Map<String, Object> params = new HashMap<>(parameters);

    try {
      ResponseEntity<T[]> responseEntity = restTemplate.exchange(buildUri(url, params),
          HttpMethod.GET, createEntity(), getArrayResultClass());

      return new ArrayList<>(Arrays.asList(responseEntity.getBody()));
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected <P> ServiceResponse<List<P>> tryFindAll(String resourceUrl, Class<P[]> type,
      String etag) {
    String url = getServiceUrl() + getUrl() + resourceUrl;
    logger.info("permissionStrings url: {}", url);

    try {
      RequestHeaders headers = RequestHeaders.init().setIfNoneMatch(etag);
      ResponseEntity<P[]> response = restTemplate.exchange(
          url, HttpMethod.GET, RequestHelper.createEntity(null, addAuthHeader(headers)), type
      );
      logger.info("permissionStrings responseEntity: {}", response);

      if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
        return new ServiceResponse<>(null, response.getHeaders(), false);
      } else {
        List<P> list = Stream.of(response.getBody()).collect(Collectors.toList());
        return new ServiceResponse<>(list, response.getHeaders(), true);
      }
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with POST request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param payload     body to include with the outgoing request.
   * @return Page of reference data T objects.
   */
  public Page<T> getPage(String resourceUrl, Map<String, Object> parameters, Object payload) {
    return getPage(resourceUrl, parameters, payload, HttpMethod.POST, getResultClass());
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with GET request.
   *
   * @param parameters  Map of query parameters.
   * @return Page of reference data T objects.
   */
  public Page<T> getPage(RequestParameters parameters) {
    return getPage("", parameters, null, HttpMethod.GET, getResultClass());
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with GET request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return Page of reference data T objects.
   */
  public Page<T> getPage(String resourceUrl, RequestParameters parameters) {
    return getPage(resourceUrl, parameters, null, HttpMethod.GET, getResultClass());
  }

  protected Page<T> getPage(Map<String, Object> parameters) {
    return getPage("", parameters);
  }

  protected Page<T> getPage(String resourceUrl, Map<String, Object> parameters) {
    return getPage(resourceUrl, parameters, null, HttpMethod.GET, getResultClass());
  }

  protected <P> Page<P> getPage(String resourceUrl, Map<String, Object> parameters, Object payload,
      HttpMethod method, Class<P> type) {
    RequestParameters params = RequestParameters.init();
    parameters.forEach(params::set);

    return getPage(resourceUrl, params, payload, method, type);
  }

  protected <P> Page<P> getPage(String resourceUrl, RequestParameters parameters, Object payload,
      HttpMethod method, Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    try {
      ResponseEntity<PageImplRepresentation<P>> response = restTemplate.exchange(
          createUri(url, parameters),
          method,
          createEntity(payload),
          new DynamicPageTypeReference<>(type)
      );
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected <P> ResultDto<P> getResult(String resourceUrl, RequestParameters parameters,
      Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    ResponseEntity<ResultDto<P>> response = restTemplate.exchange(
        createUri(url, parameters),
        HttpMethod.GET,
        createEntity(),
        new DynamicParametrizedTypeReference<>(type));

    return response.getBody();
  }

  private URI buildUri(String url, Map<String, Object> params) {
    return createUri(url, RequestParameters.of(params));
  }

  private DataRetrievalException buildDataRetrievalException(HttpStatusCodeException ex) {
    return new DataRetrievalException(getResultClass().getSimpleName(), ex);
  }

  private <E> HttpEntity<E> createEntity(E payload) {
    if (payload == null) {
      return createEntity();
    } else {
      return RequestHelper.createEntity(payload, createHeadersWithAuth());
    }
  }

  private <E> HttpEntity<E> createEntity() {
    return RequestHelper.createEntity(createHeadersWithAuth());
  }

  private RequestHeaders addAuthHeader(RequestHeaders headers) {
    return null == headers
        ? RequestHeaders.init().setAuth(authService.obtainAccessToken())
        : headers.setAuth(authService.obtainAccessToken());
  }

  private RequestHeaders createHeadersWithAuth() {
    return RequestHeaders.init().setAuth(authService.obtainAccessToken());
  }
}
