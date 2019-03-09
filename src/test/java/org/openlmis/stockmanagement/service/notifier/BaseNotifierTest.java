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

package org.openlmis.stockmanagement.service.notifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class BaseNotifierTest {

  static final String FACILITY_NAME = "Mock Facility";
  static final String PROGRAM_NAME = "Mock Program";
  static final String ORDERABLE_NAME = "Mock Orderable";
  static final String LOT_CODE = "LOT 111";
  static final String URL_TO_VIEW_BIN_CARD =
      "/stockCardSummaries/{0}";

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private BaseNotifier baseNotifier;
  
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID stockCardId = UUID.randomUUID();

  private FacilityDto facility = mock(FacilityDto.class);
  private ProgramDto program = mock(ProgramDto.class);
  private OrderableDto orderable = mock(OrderableDto.class);

  @Before
  public void setUp() {
    when(facility.getName()).thenReturn(FACILITY_NAME);
    when(program.getName()).thenReturn(PROGRAM_NAME);
    when(orderable.getFullProductName()).thenReturn(ORDERABLE_NAME);
    ReflectionTestUtils.setField(baseNotifier, "urlToViewBinCard",
        URL_TO_VIEW_BIN_CARD);
  }

  @Test
  public void getFacilityNameShouldGetNameFromId() {
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facility);

    assertEquals(FACILITY_NAME, baseNotifier.getFacilityName(facilityId));
  }

  @Test
  public void getProgramNameShouldGetNameFromId() {
    when(programReferenceDataService.findOne(programId)).thenReturn(program);

    assertEquals(PROGRAM_NAME, baseNotifier.getProgramName(programId));
  }

  @Test
  public void getOrderableNameShouldGetNameFromId() {
    when(orderableReferenceDataService.findOne(orderableId)).thenReturn(orderable);

    assertEquals(ORDERABLE_NAME, baseNotifier.getOrderableName(orderableId));
  }

  @Test
  public void getUrlToViewBinCardShouldGetUrlFromCard() {
    String urlToViewBinCard = MessageFormat.format(URL_TO_VIEW_BIN_CARD, stockCardId);

    assertEquals(urlToViewBinCard, baseNotifier.getUrlToViewBinCard(stockCardId));
  }
}
