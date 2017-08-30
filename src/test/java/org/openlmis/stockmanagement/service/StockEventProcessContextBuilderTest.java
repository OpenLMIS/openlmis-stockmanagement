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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.exception.AuthenticationException;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.UserReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoBuilder;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import java.util.ArrayList;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecurityContextHolder.class)
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

  @Mock
  private SecurityContext securityContext;

  @Mock
  private OAuth2Authentication authentication;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @InjectMocks
  private StockEventProcessContextBuilder contextBuilder;

  private UserDto userDto = new UserDto();

  @Before
  public void setUp() {
    PowerMockito.mockStatic(SecurityContextHolder.class);
    PowerMockito.when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(false);
  }

  @Test
  public void shouldBuildContextWithRefDataNeededByProcessor() throws Exception {
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);

    testBuildContext(StockEventDtoBuilder.createStockEventDto());
  }

  @Test
  public void shouldBuildContextWithUserIdFromDtoWhenClientAuthentication() throws Exception {
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setUserId(UUID.randomUUID());

    when(authentication.isClientOnly()).thenReturn(true);
    when(userReferenceDataService.findOne(stockEventDto.getUserId())).thenReturn(userDto);

    testBuildContext(stockEventDto);
  }

  @Test(expected = AuthenticationException.class)
  public void shouldThrowExceptionWhenUserIsNotFound() throws Exception {
    StockEventDto stockEventDto = StockEventDtoBuilder.createStockEventDto();
    stockEventDto.setUserId(UUID.randomUUID());

    when(authentication.isClientOnly()).thenReturn(true);
    when(userReferenceDataService.findOne(stockEventDto.getUserId())).thenReturn(null);

    testBuildContext(stockEventDto);
  }

  private void testBuildContext(StockEventDto stockEventDto) {
    //given
    UUID lotId = UUID.randomUUID();

    StockEventDtoBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setLotId(lotId);

    ProgramDto programDto = new ProgramDto();
    FacilityDto facilityDto = new FacilityDto();
    ArrayList<OrderableDto> approvedProductDtos = new ArrayList<>();

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