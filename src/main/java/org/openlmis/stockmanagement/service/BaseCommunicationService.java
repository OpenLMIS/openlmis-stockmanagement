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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.openlmis.stockmanagement.util.Merger;
import org.openlmis.stockmanagement.util.PageDto;
import org.openlmis.stockmanagement.util.RequestHelper;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class BaseCommunicationService<T> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private AuthService authService;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${request.maxUrlLength}")
  private int maxUrlLength;

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
      return runWithTokenRetry(() -> restTemplate.exchange(
          RequestHelper.createUri(url, params),
          HttpMethod.GET,
          createEntity(),
          type)).getBody();
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
    } catch (RestClientException ex) {
      throw buildDataRetrievalException(ex);
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

    RequestParameters params = RequestParameters.of(parameters);

    try {
      ResponseEntity<T[]> responseEntity = runWithTokenRetry(
          () -> doListRequest(url, params, HttpMethod.GET, getArrayResultClass())
      );
      return new ArrayList<>(Arrays.asList(responseEntity.getBody()));
    } catch (RestClientException ex) {
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
    } catch (RestClientException ex) {
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
      ResponseEntity<PageDto<P>> response = runWithTokenRetry(
          () -> doPageRequest(url, parameters, payload, method, type)
      );
      return response.getBody();

    } catch (RestClientException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected <P> ResultDto<P> getResult(String resourceUrl, RequestParameters parameters,
      Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    ResponseEntity<ResultDto<P>> response = runWithTokenRetry(() -> restTemplate.exchange(
        RequestHelper.createUri(url, parameters),
        HttpMethod.GET,
        createEntity(),
        new DynamicParametrizedTypeReference<>(type)
    ));

    return response.getBody();
  }

  protected <K, V> Map<K, V> getMap(String resourceUrl, RequestParameters parameters,
                                    Class<K> keyType, Class<V> valueType) {
    String url = getServiceUrl() + getUrl() + StringUtils.defaultIfBlank(resourceUrl, "");
    TypeFactory factory = objectMapper.getTypeFactory();
    MapType mapType = factory.constructMapType(HashMap.class, keyType, valueType);

    HttpEntity<Object> entity = createEntity();
    List<Map<K, V>> maps = new ArrayList<>();

    for (URI uri : RequestHelper.splitRequest(url, parameters, maxUrlLength)) {
      ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
      Map<K, V> map = objectMapper.convertValue(response.getBody(), mapType);
      maps.add(map);
    }

    return Merger
        .ofMaps(maps)
        .withDefaultValue(Collections::emptyMap)
        .merge();
  }

  private <E> ResponseEntity<E[]> doListRequest(String url, RequestParameters parameters,
                                                HttpMethod method, Class<E[]> type) {
    HttpEntity<Object> entity = createEntity();
    List<E[]> arrays = new ArrayList<>();

    for (URI uri : RequestHelper.splitRequest(url, parameters, maxUrlLength)) {
      arrays.add(restTemplate.exchange(uri, method, entity, type).getBody());
    }

    E[] body = Merger
        .ofArrays(arrays)
        .withDefaultValue(() -> (E[]) Array.newInstance(type.getComponentType(), 0))
        .merge();

    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  private <E> ResponseEntity<PageDto<E>> doPageRequest(String url,
                                                                      RequestParameters parameters,
                                                                      Object payload,
                                                                      HttpMethod method,
                                                                      Class<E> type) {
    HttpEntity<Object> entity = createEntity(payload);
    ParameterizedTypeReference<PageDto<E>> parameterizedType =
        new DynamicPageTypeReference<>(type);
    List<PageDto<E>> pages = new ArrayList<>();

    for (URI uri : RequestHelper.splitRequest(url, parameters, maxUrlLength)) {
      pages.add(restTemplate.exchange(uri, method, entity, parameterizedType).getBody());
    }

    PageDto<E> body = Merger
        .ofPages(pages)
        .withDefaultValue(PageDto::new)
        .merge();

    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  private DataRetrievalException buildDataRetrievalException(RestClientException ex) {
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

  protected <P> ResponseEntity<P> runWithTokenRetry(HttpTask<P> task) {
    try {
      return task.run();
    } catch (HttpStatusCodeException ex) {
      if (HttpStatus.UNAUTHORIZED == ex.getStatusCode()) {
        // the token has (most likely) expired - clear the cache and retry once
        authService.clearTokenCache();
        return task.run();
      }
      throw ex;
    }
  }

  @FunctionalInterface
  protected interface HttpTask<T> {

    ResponseEntity<T> run();

  }
}
