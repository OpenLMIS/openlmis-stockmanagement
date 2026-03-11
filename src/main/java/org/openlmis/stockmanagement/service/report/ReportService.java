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

package org.openlmis.stockmanagement.service.report;

import java.net.URI;
import java.util.Map;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.service.AuthService;
import org.openlmis.stockmanagement.service.RequestHeaders;
import org.openlmis.stockmanagement.util.RequestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * The type Report service.
 */
@Service
public class ReportService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${report.url}")
  private String reportUrl;

  @Autowired
  private AuthService authService;

  private final RestOperations restTemplate = new RestTemplate();

  /**
   * Generate report byte [ ].
   *
   * @param jasperTemplate the jasper template
   * @param params         the params
   * @return the byte [ ]
   */
  public byte[] generateReport(JasperTemplate jasperTemplate, Map<String, Object> params) {
    return fillAndExportReport(jasperTemplate.getName(), jasperTemplate.getData(), params);
  }

  /**
   * Generate report byte [ ].
   *
   * @param reportName the report name
   * @param reportData the report data
   * @param params     the params
   * @return the byte [ ]
   */
  public byte[] fillAndExportReport(String reportName, byte[] reportData,
                                    Map<String, Object> params) {
    GenerateReportDto request = buildGenerateReportRequest(reportName, reportData, params);
    logger.debug("Sending generateReport request: {}", request);

    String url = reportUrl + "/api/reports/generate";
    try {
      RequestHeaders headers;
      headers = RequestHeaders.init().setAuth(authService.obtainAccessToken());
      URI uri = RequestHelper.createUri(url);
      HttpEntity<GenerateReportDto> entity = RequestHelper.createEntity(request, headers);

      ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.POST, entity,
          byte[].class);
      return response.getBody();
    } catch (HttpStatusCodeException ex) {
      logger.error(
          "Unable to generate report. Error code: {}, response message: {}",
          ex.getStatusCode(), ex.getResponseBodyAsString()
      );
    }
    return null;
  }

  private GenerateReportDto buildGenerateReportRequest(String name, byte[] data, Map<String,
      Object> params) {
    return new GenerateReportDto(name, data, params);
  }
}
