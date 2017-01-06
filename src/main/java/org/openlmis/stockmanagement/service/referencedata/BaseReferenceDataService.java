package org.openlmis.stockmanagement.service.referencedata;

import org.openlmis.stockmanagement.service.BaseCommunicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseReferenceDataService<T> extends BaseCommunicationService {

  @Value("${referencedata.url}")
  private String referenceDataUrl;

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
   * @param resourceUrl Endpoint url.
   * @param uriParameters  Map of query parameters.
   * @param payload  body to include with the outgoing request.
   * @return all reference data T objects.
   */
  public Collection<T> postFindAll(String resourceUrl, Map<String, Object> uriParameters,
                                   Map<String, Object> payload) {
    return findAllWithMethod(resourceUrl, uriParameters, payload, HttpMethod.POST);
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

  <P> P get(Class<P> type, String resourceUrl, Map<String, Object> parameters) {
    String url = getReferenceDataUrl() + getUrl() + resourceUrl;
    Map<String, Object> params = new HashMap<>();
    params.putAll(parameters);
    params.put(ACCESS_TOKEN, obtainAccessToken());

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<P> response = restTemplate.getForEntity(buildUri(url, params), type);

    return response.getBody();
  }

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  protected String getReferenceDataUrl() {
    return referenceDataUrl;
  }

  private ReferenceDataRetrievalException buildRefDataException(HttpStatusCodeException ex) {
    return new ReferenceDataRetrievalException(getResultClass().getSimpleName(),
        ex.getStatusCode(),
        ex.getResponseBodyAsString());
  }
}
