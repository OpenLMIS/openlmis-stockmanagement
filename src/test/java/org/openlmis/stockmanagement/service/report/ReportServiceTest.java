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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.service.AuthService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {
  private static final String ACCESS_TOKEN = "token";
  private static final String BASE_URL = "https://localhost";
  private static final String REPORT_URL = BASE_URL + "/api/reports/generate";
  private static final String TEMPLATE_NAME = "templateName";
  private static final byte[] TEMPLATE_DATA = "templateData".getBytes();
  private static final byte[] REPORT_RESULT = "reportResult".getBytes();

  @Mock
  private AuthService authService;

  @Mock
  private RestOperations restTemplate;

  @InjectMocks
  private ReportService reportService;

  @Captor
  private ArgumentCaptor<HttpEntity<GenerateReportDto>> entityCaptor;

  @Before
  public void setUp() {
    when(authService.obtainAccessToken()).thenReturn(ACCESS_TOKEN);
    ReflectionTestUtils.setField(reportService, "reportUrl", BASE_URL);
    ReflectionTestUtils.setField(reportService, "restTemplate", restTemplate);
  }

  @Test
  public void shouldGenerateReportFromTemplate() {
    JasperTemplate template = mock(JasperTemplate.class);
    when(template.getName()).thenReturn(TEMPLATE_NAME);
    when(template.getData()).thenReturn(TEMPLATE_DATA);

    Map<String, Object> params = new HashMap<>();
    params.put("param1", "value1");

    ResponseEntity<byte[]> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(REPORT_RESULT);
    when(restTemplate.exchange(
        any(URI.class),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(byte[].class)
    )).thenReturn(response);

    byte[] result = reportService.generateReport(template, params);

    assertThat(result, is(REPORT_RESULT));
    verify(restTemplate).exchange(
        eq(URI.create(REPORT_URL)),
        eq(HttpMethod.POST),
        entityCaptor.capture(),
        eq(byte[].class)
    );

    GenerateReportDto request = entityCaptor.getValue().getBody();
    assertThat(request.getName(), is(TEMPLATE_NAME));
    assertThat(request.getTemplate(), is(TEMPLATE_DATA));
    assertThat(request.getParams(), is(params));
  }

  @Test
  public void shouldFillAndExportReport() {
    Map<String, Object> params = new HashMap<>();
    params.put("key", "value");

    ResponseEntity<byte[]> response = mock(ResponseEntity.class);
    when(response.getBody()).thenReturn(REPORT_RESULT);
    when(restTemplate.exchange(
        any(URI.class),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(byte[].class)
    )).thenReturn(response);

    String reportName = "report";
    byte[] reportData = "data".getBytes();
    byte[] result = reportService.fillAndExportReport(reportName, reportData, params);

    assertThat(result, is(REPORT_RESULT));
    verify(restTemplate).exchange(
        eq(URI.create(REPORT_URL)),
        eq(HttpMethod.POST),
        entityCaptor.capture(),
        eq(byte[].class)
    );

    GenerateReportDto request = entityCaptor.getValue().getBody();
    assertThat(request.getName(), is(reportName));
    assertThat(request.getTemplate(), is(reportData));
    assertThat(request.getParams(), is(params));
  }

  @Test
  public void shouldReturnNullOnHttpError() {
    String reportName = "report";
    byte[] reportData = "data".getBytes();
    Map<String, Object> params = new HashMap<>();

    when(restTemplate.exchange(
        any(URI.class),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(byte[].class)
    )).thenThrow(new HttpStatusCodeException(
        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Error") {});

    byte[] result = reportService.fillAndExportReport(reportName, reportData, params);

    assertThat(result, is(org.hamcrest.Matchers.nullValue()));
  }
}
