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

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_CARDS_VIEW;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.PermissionStringDto;
import org.openlmis.stockmanagement.service.referencedata.PermissionStrings;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDtoDataBuilder;
import org.openlmis.stockmanagement.util.AuthenticationHelper;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@RunWith(MockitoJUnitRunner.class)
public class StockCardServiceTest {

  @Mock
  private StockCardRepository cardRepository;

  @Mock
  private FacilityReferenceDataService facilityRefDataService;

  @Mock
  private ProgramReferenceDataService programRefDataService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private StockCardService stockCardService;

  @Captor
  private ArgumentCaptor<List<StockCard>> cardCaptor;

  @Mock
  private PermissionStrings.Handler permissionStringsHandler;
  
  @Mock
  private CalculatedStockOnHandService calculatedStockOnHandService;

  @Mock
  private Pageable pageable;

  @Mock
  private UserDto user;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private OAuth2Authentication authentication;

  private UUID id = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private Set<UUID> ids = Collections.singleton(id);
  private Set<UUID> facilityIds = Collections.singleton(facilityId);
  private Set<UUID> programIds = Collections.singleton(programId);
  private UUID userId = UUID.randomUUID();
  private StockCard stockCard;

  @Before
  public void setUp() {
    when(user.getId()).thenReturn(userId);
    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    when(permissionStringsHandler.get())
        .thenReturn(Collections.singleton(
            PermissionStringDto.create(
                STOCK_CARDS_VIEW,
                facilityId,
                programId)));
    when(permissionService.getPermissionStrings(userId))
        .thenReturn(permissionStringsHandler);

    when(facilityRefDataService.findOne(facilityId))
        .thenReturn(FacilityDto.builder().id(facilityId).build());
    when(programRefDataService.findOne(programId))
        .thenReturn(ProgramDto.builder().id(programId).build());

    StockEvent originalEvent = new StockEventDataBuilder()
        .withFacility(facilityId).withProgram(programId).build();
    stockCard = new StockCardDataBuilder(originalEvent).build();

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(false);
  }

  @Test
  public void shouldNotDuplicateCardsForOrderableLots() {
    StockEventDto event = StockEventDtoDataBuilder.createStockEventDtoWithTwoLineItems();
    event.setContext(mock(StockEventProcessContext.class));

    UUID savedEventId = UUID.randomUUID();

    stockCardService.saveFromEvent(event, savedEventId);

    verify(cardRepository).saveAll(cardCaptor.capture());

    List<StockCard> saved = cardCaptor.getValue();

    assertThat(saved, hasSize(1));

    StockCard card = saved.get(0);

    assertThat(card.getFacilityId(), equalTo(event.getFacilityId()));
    assertThat(card.getProgramId(), equalTo(event.getProgramId()));

    assertThat(card.getOrderableId(), equalTo(event.getLineItems().get(0).getOrderableId()));
    assertThat(card.getLotId(), equalTo(event.getLineItems().get(0).getLotId()));

    assertThat(card.getOrderableId(), equalTo(event.getLineItems().get(1).getOrderableId()));
    assertThat(card.getLotId(), equalTo(event.getLineItems().get(1).getLotId()));
    assertThat(card.getLineItems(), hasSize(2));
  }

  @Test
  public void shouldGetStockCardsBasedOnPermissionString() {
    when(cardRepository.findByFacilityIdInAndProgramIdIn(facilityIds, programIds, pageable))
        .thenReturn(
            new PageImpl<>(Collections.singletonList(stockCard), Pageable.unpaged(), 10));

    Page<StockCardDto> stockCardDtoPage = stockCardService.search(emptyList(), pageable);

    verifyPage(stockCardDtoPage);
  }

  @Test
  public void shouldGetStockCardsBasedOnPermissionStringAndIds() {
    when(cardRepository
        .findByFacilityIdInAndProgramIdInAndIdIn(facilityIds, programIds, ids, pageable))
        .thenReturn(
            new PageImpl<>(Collections.singletonList(stockCard), Pageable.unpaged(), 10));

    Page<StockCardDto> stockCardDtoPage =
        stockCardService.search(ids, pageable);

    verifyPage(stockCardDtoPage);
  }

  @Test
  public void shouldGetAllStockCardsIfClientCallsAndIdsEmpty() {
    when(authentication.isClientOnly()).thenReturn(true);
    when(cardRepository
        .findAll(pageable))
        .thenReturn(
            new PageImpl<>(Collections.singletonList(stockCard), Pageable.unpaged(), 10));

    Page<StockCardDto> stockCardDtoPage =
        stockCardService.search(emptyList(), pageable);

    verifyPage(stockCardDtoPage);
  }

  @Test
  public void shouldGetAllStockCardsByIdIfClientCallsAndIdsNotEmpty() {
    when(authentication.isClientOnly()).thenReturn(true);
    when(cardRepository
        .findByIdIn(ids, pageable))
        .thenReturn(
            new PageImpl<>(Collections.singletonList(stockCard), Pageable.unpaged(), 10));

    Page<StockCardDto> stockCardDtoPage =
        stockCardService.search(ids, pageable);

    verifyPage(stockCardDtoPage);
  }

  private void verifyPage(Page<StockCardDto> search) {
    assertEquals(10, search.getTotalElements());
    assertEquals(1, search.getNumberOfElements());
    StockCardDto stockCardDto = search.getContent().get(0);
    assertEquals(stockCard.getId(), stockCardDto.getId());
    assertEquals(facilityId, stockCardDto.getFacility().getId());
    assertEquals(programId, stockCardDto.getProgram().getId());
    assertEquals(stockCard.getOrderableId(), stockCardDto.getOrderableId());
    assertEquals(stockCard.getLotId(), stockCardDto.getLotId());
    verify(calculatedStockOnHandService, times(1))
        .fetchCurrentStockOnHand(any(StockCard.class));
  }
}
