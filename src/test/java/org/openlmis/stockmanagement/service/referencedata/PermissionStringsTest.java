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

package org.openlmis.stockmanagement.service.referencedata;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.service.ServiceResponse;

@RunWith(MockitoJUnitRunner.class)
public class PermissionStringsTest {
  private static final String HANDLERS_FIELD_NAME = "handlers";
  private static final UUID USER = UUID.randomUUID();

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @InjectMocks
  private PermissionStrings permissionStrings;

  @Mock
  private ServiceResponse<List<String>> response;

  @Test
  public void shouldCreateHandlerIfNotExist() throws Exception {
    Field handlers = PermissionStrings.class.getDeclaredField(HANDLERS_FIELD_NAME);
    handlers.setAccessible(true);

    Map map = (Map) handlers.get(permissionStrings);
    assertThat(map.size(), is(0));

    PermissionStrings.Handler handler = permissionStrings.forUser(USER);

    assertThat(handler, is(notNullValue()));

    map = (Map) handlers.get(permissionStrings);
    assertThat(map.size(), is(1));
  }

  @Test
  public void shouldNotRecreateHandler() throws Exception {
    Field handlers = PermissionStrings.class.getDeclaredField(HANDLERS_FIELD_NAME);
    handlers.setAccessible(true);

    permissionStrings.forUser(USER);
    permissionStrings.forUser(USER);
    permissionStrings.forUser(USER);
    permissionStrings.forUser(USER);

    Map map = (Map) handlers.get(permissionStrings);
    assertThat(map.size(), is(1));
  }

  @Test
  public void shouldUpdateDataIfResponseWasModified() throws Exception {
    String etag = random(5);
    PermissionStrings.Handler handler = permissionStrings.forUser(USER);

    // here handler does not have etag so it should pass null value
    when(userReferenceDataService.getPermissionStrings(USER, null)).thenReturn(response);
    when(response.isModified()).thenReturn(true);
    when(response.getETag()).thenReturn(etag);
    when(response.getBody()).thenReturn(singletonList(random(5)));
    Set<PermissionStringDto> one = handler.get();

    assertThat(one, hasSize(1));

    // here handler should have etag and should use it
    when(userReferenceDataService.getPermissionStrings(USER, etag)).thenReturn(response);
    when(response.getBody()).thenReturn(singletonList(random(5)));
    Set<PermissionStringDto> two = handler.get();

    assertThat(two, hasSize(1));

    assertThat(one, is(not(equalTo(two))));
  }

  @Test
  public void shouldNotUpdateDataIfResponseWasNotModified() throws Exception {
    String etag = random(5);
    PermissionStrings.Handler handler = permissionStrings.forUser(USER);

    // here handler does not have etag so it should pass null value
    when(userReferenceDataService.getPermissionStrings(USER, null)).thenReturn(response);
    when(response.isModified()).thenReturn(true);
    when(response.getETag()).thenReturn(etag);
    when(response.getBody()).thenReturn(singletonList(random(5)));
    Set<PermissionStringDto> one = handler.get();

    assertThat(one, hasSize(1));

    // here handler should have etag and should use it
    when(userReferenceDataService.getPermissionStrings(USER, etag)).thenReturn(response);
    when(response.isModified()).thenReturn(false);
    Set<PermissionStringDto> two = handler.get();

    assertThat(two, hasSize(1));

    assertThat(one, is(equalTo(two)));
  }
}
