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

import java.util.ArrayList;
import java.util.UUID;
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
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecurityContextHolder.class)
@PowerMockIgnore("javax.security.auth.*")
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
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private OAuth2Authentication authentication;

  @InjectMocks
  private StockEventProcessContextBuilder contextBuilder;

  private UserDto userDto = new UserDto();

  @Before
  public void setUp() {
    userDto.setId(UUID.randomUUID());

    PowerMockito.mockStatic(SecurityContextHolder.class);
    PowerMockito.when(SecurityContextHolder.getContext()).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(false);
  }

  @Test
  public void shouldBuildContextWithRefDataNeededByProcessor() throws Exception {
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);

    testBuildContext(StockEventDtoDataBuilder.createStockEventDto());
  }

  @Test
  public void shouldBuildContextWithUserIdFromDtoWhenClientAuthentication() throws Exception {
    StockEventDto stockEventDto = StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.setUserId(userDto.getId());

    when(authentication.isClientOnly()).thenReturn(true);

    testBuildContext(stockEventDto);
  }

  private void testBuildContext(StockEventDto stockEventDto) {
    //given
    UUID lotId = UUID.randomUUID();

    LotDto lot = new LotDto();
    lot.setId(lotId);

    StockEventDtoDataBuilder.createStockEventDto();
    stockEventDto.getLineItems().get(0).setLotId(lotId);

    ProgramDto programDto = new ProgramDto();
    FacilityDto facilityDto = new FacilityDto();
    ArrayList<OrderableDto> approvedProductDtos = new ArrayList<>();

    when(programService.findOne(stockEventDto.getProgramId())).thenReturn(programDto);
    when(facilityService.findOne(stockEventDto.getFacilityId())).thenReturn(facilityDto);
    when(orderableReferenceDataService
        .findAll())
        .thenReturn(approvedProductDtos);
    when(lotReferenceDataService.findOne(lotId)).thenReturn(lot);

    //when
    StockEventProcessContext context = contextBuilder.buildContext(stockEventDto);

    //then
    assertThat(context.getCurrentUserId(), is(userDto.getId()));
    assertThat(context.getProgram(), is(programDto));
    assertThat(context.getFacility(), is(facilityDto));
    assertThat(context.getAllApprovedProducts(), is(approvedProductDtos));
    assertThat(context.findLot(lotId), is(lot));
  }
}