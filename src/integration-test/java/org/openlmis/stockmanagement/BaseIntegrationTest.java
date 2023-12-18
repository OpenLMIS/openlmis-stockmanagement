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

package org.openlmis.stockmanagement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.service.StockEventProcessContextBuilder;
import org.openlmis.stockmanagement.util.StockEventProcessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = {"classpath:application.properties", "classpath:test.properties"})
@ActiveProfiles("test-run")
@DirtiesContext
public abstract class BaseIntegrationTest {

  @Autowired
  private StockEventProcessContextBuilder contextBuilder;

  protected void mockAuthentication() throws Exception {
    SecurityContext securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    OAuth2Authentication authentication = mock(OAuth2Authentication.class);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isClientOnly()).thenReturn(true);
  }

  protected void setContext(StockEventDto event) {
    StockEventProcessContext context = contextBuilder.buildContext(event);
    event.setContext(context);
  }

}
