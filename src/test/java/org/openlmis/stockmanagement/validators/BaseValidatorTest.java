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

package org.openlmis.stockmanagement.validators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.StockEventProcessContextBuilder;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseValidatorTest {

  @Mock
  FacilityReferenceDataService facilityService;

  @Mock
  ProgramReferenceDataService programService;

  @Mock
  OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  LotReferenceDataService lotReferenceDataService;

  @Mock
  StockCardLineItemReasonRepository reasonRepository;

  @Mock
  NodeRepository nodeRepository;

  @Mock
  StockCardRepository stockCardRepository;

  @Mock
  ValidSourceAssignmentRepository validSourceAssignmentRepository;

  @Mock
  ValidDestinationAssignmentRepository validDestinationAssignmentRepository;

  @InjectMocks
  private StockEventProcessContextBuilder contextBuilder;

  @Before
  public void setUp() throws Exception {
    SecurityContext securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    OAuth2Authentication authentication = mock(OAuth2Authentication.class);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(true);
  }

  void setContext(StockEventDto event) {
    StockEventProcessContext context = contextBuilder.buildContext(event);
    event.setContext(context);
  }

  void setReasons(StockEventDto event, List<StockCardLineItemReason> reasons) {
    when(reasonRepository.findByIdIn(event.getReasonIds()))
        .thenReturn(reasons);
  }

}
