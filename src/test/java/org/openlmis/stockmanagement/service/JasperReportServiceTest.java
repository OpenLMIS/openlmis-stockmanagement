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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.service.JasperReportService.CARD_REPORT_URL;
import static org.openlmis.stockmanagement.service.JasperReportService.CARD_SUMMARY_REPORT_URL;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.testutils.StockCardDtoDataBuilder;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
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

  @Before
  public void setUp() {
    jasperReportService = spy(new JasperReportService());
    ReflectionTestUtils.setField(jasperReportService, "dateFormat", DATE_FORMAT);
    ReflectionTestUtils.setField(jasperReportService, "dateTimeFormat", DATE_TIME_FORMAT);
    ReflectionTestUtils.setField(jasperReportService, "groupingSeparator", GROUPING_SEPARATOR);
    ReflectionTestUtils.setField(jasperReportService, "groupingSize", GROUPING_SIZE);
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundExceptionWhenStockCardNotExists() {
    UUID stockCardId = UUID.randomUUID();
    when(stockCardService.findStockCardById(stockCardId)).thenReturn(null);

    jasperReportService.generateStockCardReport(stockCardId);
  }

  @Test
  @Ignore
  public void shouldGenerateReportWithProperParamsIfStockCardExists() throws SQLException {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();
    when(stockCardService.findStockCardById(stockCard.getId())).thenReturn(stockCard);
    Map<String, Object> params = new HashMap<>();
    params.put("datasource", singletonList(stockCard));
    params.put("hasLot", stockCard.hasLot());
    params.put("dateFormat", DATE_FORMAT);
    params.put("decimalFormat", createDecimalFormat());
    when(dataSource.getConnection()).thenReturn(Mockito.mock(Connection.class));

    jasperReportService.generateStockCardReport(stockCard.getId());

    verify(jasperReportService).fillAndExportReport(
        CARD_REPORT_URL.replace("jrxml", "jasper"), params);
  }

  @Test
  @Ignore
  public void shouldGenerateReportWithProperParamsIfStockCardSummaryExists() throws SQLException {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();

    Map<String, Object> params = new HashMap<>();
    params.put("stockCardSummaries", singletonList(stockCard));
    params.put("program", stockCard.getProgram());
    params.put("facility", stockCard.getFacility());
    params.put("showProgram", false);
    params.put("showFacility", false);
    params.put("showLot", false);
    params.put("dateFormat", DATE_FORMAT);
    params.put("dateTimeFormat", DATE_TIME_FORMAT);
    params.put("decimalFormat", createDecimalFormat());

    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(stockCardSummariesService.findStockCards(programId, facilityId))
        .thenReturn(singletonList(stockCard));
    when(dataSource.getConnection()).thenReturn(Mockito.mock(Connection.class));

    jasperReportService.generateStockCardSummariesReport(programId, facilityId);

    verify(jasperReportService).fillAndExportReport(
        CARD_SUMMARY_REPORT_URL.replace("jrxml", "jasper"), params);
  }

  private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(GROUPING_SEPARATOR.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.valueOf(GROUPING_SIZE));
    return decimalFormat;
  }
}