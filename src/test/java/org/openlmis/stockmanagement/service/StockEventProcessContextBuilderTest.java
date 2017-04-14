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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.FacilityDto;
import org.openlmis.stockmanagement.dto.LotDto;
import org.openlmis.stockmanagement.dto.ProgramDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.StockEventProcessContext;

import java.util.ArrayList;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockEventProcessContextBuilderTest {

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private FacilityReferenceDataService facilityService;

  @Mock
  private ProgramReferenceDataService programService;

  @Mock
  private LotReferenceDataService lotReferenceDataService;

  @Mock
  private ApprovedProductReferenceDataService approvedProductService;

  @InjectMocks
  private StockEventProcessContextBuilder contextBuilder;

  @Test
  public void should_build_context_with_ref_data_needed_by_processor() throws Exception {
    //given
    UUID lotId = UUID.randomUUID();

    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setLotId(lotId);

    UserDto userDto = new UserDto();
    ProgramDto programDto = new ProgramDto();
    FacilityDto facilityDto = new FacilityDto();
    ArrayList<ApprovedProductDto> approvedProductDtos = new ArrayList<>();


    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
    when(programService.findOne(stockEventDto.getProgramId())).thenReturn(programDto);
    when(facilityService.findOne(stockEventDto.getFacilityId())).thenReturn(facilityDto);
    when(approvedProductService
        .getAllApprovedProducts(stockEventDto.getProgramId(), stockEventDto.getFacilityId()))
        .thenReturn(approvedProductDtos);
    LotDto lot = new LotDto();
    when(lotReferenceDataService.findOne(lotId)).thenReturn(lot);

    //when
    StockEventProcessContext context = contextBuilder.buildContext(stockEventDto);

    //then
    assertThat(context.getCurrentUser(), is(userDto));
    assertThat(context.getProgram(), is(programDto));
    assertThat(context.getFacility(), is(facilityDto));
    assertThat(context.getAllApprovedProducts(), is(approvedProductDtos));
    assertThat(context.getLots().get(lotId), is(lot));
  }
}