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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.service.JasperReportService.CARD_REPORT_URL;
import static org.openlmis.stockmanagement.service.JasperReportService.CARD_SUMMARY_REPORT_URL;
import static org.openlmis.stockmanagement.service.JasperReportService.PI_LINES_REPORT_URL;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.testutils.StockCardDtoDataBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JasperReportService.class, JasperFillManager.class,JasperExportManager.class})
public class JasperReportServiceTest {

  private static final String DATE_FORMAT = "dd/MM/yyyy";
  private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
  private static final String GROUPING_SEPARATOR = ",";
  private static final String GROUPING_SIZE = "3";

  @InjectMocks
  private JasperReportService jasperReportService;

  @Mock
  private StockCardService stockCardService;

  @Mock
  private StockCardSummariesService stockCardSummariesService;
  
  @Mock
  private DataSource dataSource;

  private byte[] testReportData;
  
  @Before
  public void setUp() {
    jasperReportService = spy(new JasperReportService());
    ReflectionTestUtils.setField(jasperReportService, "dateFormat", DATE_FORMAT);
    ReflectionTestUtils.setField(jasperReportService, "dateTimeFormat", DATE_TIME_FORMAT);
    ReflectionTestUtils.setField(jasperReportService, "groupingSeparator", GROUPING_SEPARATOR);
    ReflectionTestUtils.setField(jasperReportService, "groupingSize", GROUPING_SIZE);
    MockitoAnnotations.initMocks(this);
    mockStatic(JasperFillManager.class);
    mockStatic(JasperExportManager.class);
    testReportData = new byte[]{0x1};
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundExceptionWhenStockCardNotExists() {
    UUID stockCardId = UUID.randomUUID();
    when(stockCardService.findStockCardById(stockCardId)).thenReturn(null);

    jasperReportService.generateStockCardReport(stockCardId);
  }

  @Test
  public void shouldGenerateReportWithProperParamsIfStockCardExists() throws JRException {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();

    when(stockCardService.findStockCardById(stockCard.getId())).thenReturn(stockCard);

    when(jasperReportService.compileReportFromTemplateUrl(CARD_REPORT_URL))
        .thenReturn(mock(JasperReport.class));
    JasperPrint jasperPrint = mock(JasperPrint.class);
    PowerMockito.when(JasperFillManager.fillReport(any(JasperReport.class), anyMap(),
        any(JRBeanCollectionDataSource.class)))
        .thenReturn(jasperPrint);
    PowerMockito.when(JasperExportManager.exportReportToPdf(jasperPrint))
        .thenReturn(testReportData);

    byte[] reportData = jasperReportService.generateStockCardReport(stockCard.getId());

    assertEquals(testReportData, reportData);
  }

  @Test
  public void shouldGenerateReportWithProperParamsIfStockCardSummaryExists() throws JRException {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();

    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(stockCardSummariesService.findStockCards(programId, facilityId))
        .thenReturn(singletonList(stockCard));

    when(jasperReportService.compileReportFromTemplateUrl(CARD_SUMMARY_REPORT_URL))
        .thenReturn(mock(JasperReport.class));
    JasperPrint jasperPrint = mock(JasperPrint.class);
    PowerMockito.when(JasperFillManager.fillReport(any(JasperReport.class), anyMap(),
        any(JREmptyDataSource.class)))
        .thenReturn(jasperPrint);
    PowerMockito.when(JasperExportManager.exportReportToPdf(jasperPrint))
        .thenReturn(testReportData);

    byte[] reportData = jasperReportService.generateStockCardSummariesReport(programId, facilityId);

    assertEquals(testReportData, reportData);
  }

  @Test
  public void shouldGenerateReportWithProperParamsForPrintPhysicalInventory() throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("dateTimeFormat", DATE_TIME_FORMAT);
    params.put("dateFormat", DATE_FORMAT);
    params.put("format", "pdf");
    params.put("decimalFormat", createDecimalFormat());

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(JasperCompileManager.compileReport(getClass().getResourceAsStream(
        PI_LINES_REPORT_URL)));
    JasperTemplate jasperTemplate = new JasperTemplate("test template", bos.toByteArray(), "type",
        "description");
    when(dataSource.getConnection()).thenReturn(mock(Connection.class));
    JasperPrint jasperPrint = mock(JasperPrint.class);
    PowerMockito.when(JasperFillManager.fillReport(any(JasperReport.class), anyMap(),
        any(Connection.class)))
        .thenReturn(jasperPrint);
    PowerMockito.when(JasperExportManager.exportReportToPdf(jasperPrint))
        .thenReturn(testReportData);

    byte[] reportData = jasperReportService.generateReport(jasperTemplate, params);

    assertEquals(testReportData, reportData);
  }

  @Test(expected = JasperReportViewException.class)
  public void shouldThrowJasperReportViewExceptionWhenConnectionCantBeOpen() throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("format", "pdf");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(JasperCompileManager.compileReport(getClass().getResourceAsStream(
        PI_LINES_REPORT_URL)));
    JasperTemplate jasperTemplate = new JasperTemplate("test template", bos.toByteArray(), "type",
        "description");

    when(dataSource.getConnection()).thenThrow(new SQLException());
    jasperReportService.generateReport(jasperTemplate, params);
  }

  private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(GROUPING_SEPARATOR.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.parseInt(GROUPING_SIZE));
    return decimalFormat;
  }
}