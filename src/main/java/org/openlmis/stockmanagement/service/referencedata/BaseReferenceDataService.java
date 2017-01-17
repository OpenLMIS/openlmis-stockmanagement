package org.openlmis.stockmanagement.service.referencedata;

import org.openlmis.stockmanagement.dto.ResultDto;
import org.openlmis.stockmanagement.service.BaseCommunicationService;
import org.openlmis.stockmanagement.utils.DynamicParametrizedTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BaseReferenceDataService<T> extends BaseCommunicationService {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${referencedata.url}")
  private String referenceDataUrl;


  /**
   * Return one object from Reference data service.
   *
   * @param id UUID of requesting object.
   * @return Requesting reference data object.
   */
  public T findOne(UUID id) {
    String url = getReferenceDataUrl() + getUrl() + id;

    RestTemplate restTemplate = new RestTemplate();
    Map<String, String> params = new HashMap<>();
    params.put(ACCESS_TOKEN, obtainAccessToken());

    try {
      ResponseEntity<T> responseEntity = restTemplate.exchange(
              buildUri(url, params), HttpMethod.GET, null, getResultClass());
      return responseEntity.getBody();
    } catch (HttpStatusCodeException ex) {
      // rest template will handle 404 as an exception, instead of returning null
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn("{} with id {} does not exist. ", getResultClass().getSimpleName(), id);
        return null;
      } else {
        throw buildRefDataException(ex);
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
  public Collection<T> findAll(String resourceUrl, Map<String, Object> parameters) {
    return findAllWithMethod(resourceUrl, parameters, null, HttpMethod.GET);
  }

  /**
   * Return all reference data T objects that need to be retrieved with POST request.
   *
   * @param resourceUrl   Endpoint url.
   * @param uriParameters Map of query parameters.
   * @param payload       body to include with the outgoing request.
   * @return all reference data T objects.
   */
  public Collection<T> postFindAll(String resourceUrl, Map<String, Object> uriParameters,
                                   Map<String, Object> payload) {
    return findAllWithMethod(resourceUrl, uriParameters, payload, HttpMethod.POST);
  }

  <P> ResultDto<P> getValue(String resourceUrl, Map<String, Object> parameters, Class<P> type) {
    String url = getReferenceDataUrl() + getUrl() + resourceUrl;
    Map<String, Object> params = new HashMap<>();
    params.putAll(parameters);
    params.put(ACCESS_TOKEN, obtainAccessToken());

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<ResultDto<P>> response = restTemplate.exchange(
            buildUri(url, params),
            HttpMethod.GET,
            null,
            new DynamicParametrizedTypeReference<>(type)
    );

    return response.getBody();
  }

  private Collection<T> findAllWithMethod(String resourceUrl, Map<String, Object> uriParameters,
                                          Map<String, Object> payload, HttpMethod method) {
    String url = getReferenceDataUrl() + getUrl() + resourceUrl;
    RestTemplate restTemplate = new RestTemplate();

    Map<String, Object> params = new HashMap<>();
    params.put(ACCESS_TOKEN, obtainAccessToken());
    params.putAll(uriParameters);

    try {
      ResponseEntity<T[]> responseEntity;
      if (HttpMethod.GET == method) {
        responseEntity = restTemplate.getForEntity(buildUri(url, params), getArrayResultClass());
      } else {
        responseEntity = restTemplate.postForEntity(buildUri(url, params), payload,
                getArrayResultClass());
      }
      return new ArrayList<>(Arrays.asList(responseEntity.getBody()));
    } catch (HttpStatusCodeException ex) {
      throw buildRefDataException(ex);
    }
  }

  protected abstract String getUrl();


  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  protected String getReferenceDataUrl() {
    return referenceDataUrl;
  }

  private ReferenceDataRetrievalException buildRefDataException(
          HttpStatusCodeException ex) {
    // TODO: replace with whatever error handling we decide on
    return new ReferenceDataRetrievalException(getResultClass().getSimpleName(),
            ex.getStatusCode(),
            ex.getResponseBodyAsString());
  }
}
