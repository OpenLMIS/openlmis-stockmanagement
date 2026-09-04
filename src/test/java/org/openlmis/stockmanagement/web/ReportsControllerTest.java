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

package org.openlmis.stockmanagement.web;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class ReportsControllerTest {

  private static final String LANG = "en";

  @Mock
  private JasperReportService reportService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private StockEventsRepository stockEventsRepository;

  @InjectMocks
  private ReportsController reportsController;

  @Test
  public void shouldReturnStockEventReportInPdf() {
    UUID stockEventId = UUID.randomUUID();
    StockEvent stockEvent = new StockEventDataBuilder().build();
    byte[] report = new byte[]{0x1, 0x2, 0x3};

    when(stockEventsRepository.findById(stockEventId)).thenReturn(Optional.of(stockEvent));
    when(reportService.generateStockEventReport(stockEventId, LANG, true)).thenReturn(report);

    ResponseEntity<byte[]> response = reportsController.print(stockEventId, true, LANG);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
    assertEquals("inline; filename=stock_event_" + stockEventId + ".pdf",
        response.getHeaders().getFirst("Content-Disposition"));
    assertArrayEquals(report, response.getBody());

    verify(permissionService)
        .canViewStockCard(stockEvent.getProgramId(), stockEvent.getFacilityId());
    verify(reportService).generateStockEventReport(stockEventId, LANG, true);
  }

  @Test
  public void shouldPassRequestedLanguageToReportService() {
    UUID stockEventId = UUID.randomUUID();
    StockEvent stockEvent = new StockEventDataBuilder().build();

    when(stockEventsRepository.findById(stockEventId)).thenReturn(Optional.of(stockEvent));
    when(reportService.generateStockEventReport(eq(stockEventId), anyString(), any(Boolean.class)))
        .thenReturn(new byte[]{0x1});

    reportsController.print(stockEventId, true, "fr");

    verify(reportService).generateStockEventReport(stockEventId, "fr", true);
  }

  @Test
  public void shouldPassShowInPacksFlagToReportService() {
    UUID stockEventId = UUID.randomUUID();
    StockEvent stockEvent = new StockEventDataBuilder().build();

    when(stockEventsRepository.findById(stockEventId)).thenReturn(Optional.of(stockEvent));
    when(reportService.generateStockEventReport(eq(stockEventId), anyString(), any(Boolean.class)))
        .thenReturn(new byte[]{0x1});

    reportsController.print(stockEventId, false, LANG);

    verify(reportService).generateStockEventReport(stockEventId, LANG, false);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowExceptionWhenStockEventDoesNotExist() {
    UUID stockEventId = UUID.randomUUID();
    when(stockEventsRepository.findById(stockEventId)).thenReturn(Optional.empty());

    try {
      reportsController.print(stockEventId, true, LANG);
    } finally {
      verify(permissionService, never()).canViewStockCard(any(UUID.class), any(UUID.class));
      verify(reportService, never())
          .generateStockEventReport(any(UUID.class), anyString(), any(Boolean.class));
    }
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldThrowExceptionWhenUserHasNoPermissionToViewReport() {
    UUID stockEventId = UUID.randomUUID();
    StockEvent stockEvent = new StockEventDataBuilder().build();

    when(stockEventsRepository.findById(stockEventId)).thenReturn(Optional.of(stockEvent));
    doThrow(new PermissionMessageException(new Message("no.permission")))
        .when(permissionService)
        .canViewStockCard(stockEvent.getProgramId(), stockEvent.getFacilityId());

    try {
      reportsController.print(stockEventId, true, LANG);
    } finally {
      verify(reportService, never())
          .generateStockEventReport(any(UUID.class), anyString(), any(Boolean.class));
    }
  }
}
