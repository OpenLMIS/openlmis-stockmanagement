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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.testutils.StockCardDtoDataBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;

@RunWith(MockitoJUnitRunner.class)
public class JasperReportServiceTest {

  @InjectMocks
  private JasperReportService jasperReportService;

  @Mock
  private StockCardService stockCardService;

  @Mock
  private StockCardSummariesService stockCardSummariesService;

  @Mock
  private JasperReportsPdfView jasperReportsPdfView;

  @Value("${dateFormat}")
  private String dateFormat;

  @Value("${dateTimeFormat}")
  private String dateTimeFormat;

  @Before
  public void setUp() {
    jasperReportService = spy(new JasperReportService());
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundExceptionWhenStockCardNotExists() {
    UUID stockCardId = UUID.randomUUID();
    when(stockCardService.findStockCardById(stockCardId)).thenReturn(null);

    jasperReportService.getStockCardReportView(stockCardId);
  }

  @Test
  public void shouldGenerateReportWithProperParamsIfStockCardExists() throws Exception {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();
    when(stockCardService.findStockCardById(stockCard.getId())).thenReturn(stockCard);

    doReturn(jasperReportsPdfView).when(jasperReportService).createJasperReportsPdfView();

    ModelAndView report = jasperReportService.getStockCardReportView(stockCard.getId());
    Map<String, Object> outputParams = report.getModel();

    assertEquals(singletonList(stockCard), outputParams.get("datasource"));
    assertEquals(stockCard.hasLot(), outputParams.get("hasLot"));
    assertEquals(dateFormat, outputParams.get("dateFormat"));
  }

  @Test
  public void shouldGenerateReportWithProperParamsIfStockCardSummaryExists() throws Exception {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    doReturn(jasperReportsPdfView).when(jasperReportService).createJasperReportsPdfView();

    when(stockCardSummariesService.findStockCards(programId, facilityId))
        .thenReturn(singletonList(stockCard));

    ModelAndView report = jasperReportService
        .getStockCardSummariesReportView(programId, facilityId);
    Map<String, Object> outputParams = report.getModel();

    assertEquals(singletonList(stockCard), outputParams.get("stockCardSummaries"));
    assertEquals(stockCard.getProgram(), outputParams.get("program"));
    assertEquals(stockCard.getFacility(), outputParams.get("facility"));
    assertEquals(dateTimeFormat, outputParams.get("dateTimeFormat"));
    assertEquals(dateFormat, outputParams.get("dateFormat"));
    assertEquals(false, outputParams.get("showProgram"));
    assertEquals(false, outputParams.get("showFacility"));
    assertEquals(false, outputParams.get("showLot"));
  }
}