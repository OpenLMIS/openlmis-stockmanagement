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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.EventOrigin;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.extension.ExtensionManager;
import org.openlmis.stockmanagement.extension.point.StockEventPostProcessor;
import org.openlmis.stockmanagement.repository.StockEventsRepository;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

@RunWith(MockitoJUnitRunner.class)
public class StockEventProcessorDocumentNumberTest {

  @Mock
  private StockEventProcessContextBuilder contextBuilder;

  @Mock
  private StockEventValidationsService stockEventValidationsService;

  @Mock
  private StockCardService stockCardService;

  @Mock
  private StockEventsRepository stockEventsRepository;

  @Mock
  private StockEventNotificationProcessor stockEventNotificationProcessor;

  @Mock
  private ExtensionManager extensionManager;

  @Mock
  private StockEventPostProcessor postProcessor;

  @Mock
  private DocumentNumberGenerator documentNumberGenerator;

  @InjectMocks
  private StockEventProcessor processor;

  @Mock
  private StockEventProcessContext context;

  @Before
  public void setUp() {
    when(contextBuilder.buildContext(any(StockEventDto.class))).thenReturn(context);
    when(context.getCurrentUserId()).thenReturn(UUID.randomUUID());
    when(extensionManager.getExtension(any(String.class), any()))
        .thenReturn(postProcessor);
    when(stockEventsRepository.save(any(StockEvent.class)))
        .thenAnswer(invocation -> {
          StockEvent saved = (StockEvent) invocation.getArguments()[0];
          saved.setId(UUID.randomUUID());
          return saved;
        });
  }

  @Test
  public void generatesDocumentNumberWhenEventOriginIsIssueAndDocumentNumberBlank() {
    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDto();
    eventDto.setEventOrigin(EventOrigin.ISSUE);
    eventDto.setDocumentNumber(null);
    when(documentNumberGenerator.generate(eventDto.getFacilityId()))
        .thenReturn("2026-05-FAC001-0001");

    processor.process(eventDto);

    verify(documentNumberGenerator).generate(eventDto.getFacilityId());
    assertEquals("2026-05-FAC001-0001", eventDto.getDocumentNumber());
  }

  @Test
  public void generatesDocumentNumberWhenEventOriginIsReceiveAndDocumentNumberEmpty() {
    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDto();
    eventDto.setEventOrigin(EventOrigin.RECEIVE);
    eventDto.setDocumentNumber("   ");
    when(documentNumberGenerator.generate(eventDto.getFacilityId()))
        .thenReturn("2026-05-FAC001-0002");

    processor.process(eventDto);

    assertEquals("2026-05-FAC001-0002", eventDto.getDocumentNumber());
  }

  @Test
  public void preservesCallerSuppliedDocumentNumberWhenEventOriginIsSet() {
    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDto();
    eventDto.setEventOrigin(EventOrigin.ISSUE);
    eventDto.setDocumentNumber("ORDER-123");

    processor.process(eventDto);

    verify(documentNumberGenerator, never()).generate(any(UUID.class));
    assertEquals("ORDER-123", eventDto.getDocumentNumber());
  }

  @Test
  public void doesNotGenerateDocumentNumberWhenEventOriginIsNull() {
    StockEventDto eventDto = StockEventDtoDataBuilder.createStockEventDto();
    eventDto.setEventOrigin(null);
    eventDto.setDocumentNumber(null);

    processor.process(eventDto);

    verify(documentNumberGenerator, never()).generate(any(UUID.class));
    assertNull(eventDto.getDocumentNumber());
  }
}
