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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.service.BaseCommunicationService;
import org.openlmis.stockmanagement.service.RequestHeaders;
import org.openlmis.stockmanagement.service.ServiceResponse;
import org.openlmis.stockmanagement.testutils.ObjectGenerator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserReferenceDataServiceTest extends BaseReferenceDataServiceTest<UserDto> {

  private static final String PERMISSION_STRING = "right-name|facility-id";
  private static final String ETAG_FROM_RESPONSE = "new-etag";
  private static final String ETAG = "etag";
  private static final String SEARCH = "search";

  @Mock
  private ResponseEntity<String[]> stringArrayResponse;

  private UserReferenceDataService service;
  private UUID userId = UUID.randomUUID();

  @Override
  protected BaseCommunicationService<UserDto> getService() {
    return new UserReferenceDataService();
  }

  @Override
  protected UserDto generateInstance() {
    return ObjectGenerator.of(UserDto.class);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    service = (UserReferenceDataService) prepareService();

    when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
        any(Class.class))).thenReturn(stringArrayResponse);
    when(stringArrayResponse.getBody()).thenReturn(new String[]{PERMISSION_STRING});
  }

  @Test
  public void shouldGetPermissionStringsForUser() {
    when(stringArrayResponse.getHeaders())
        .thenReturn(RequestHeaders.init().set(HttpHeaders.ETAG, ETAG_FROM_RESPONSE).toHeaders());
    when(stringArrayResponse.getStatusCode()).thenReturn(HttpStatus.OK);

    ServiceResponse<List<String>> serviceResponse =
        service.getPermissionStrings(userId, ETAG);

    assertEquals(ETAG_FROM_RESPONSE, serviceResponse.getETag());
    assertEquals(PERMISSION_STRING, serviceResponse.getBody().get(0));
    assertTrue(serviceResponse.isModified());
  }

  @Test
  public void shouldNotGetPermissionStringsIfEtagMatch() {
    when(stringArrayResponse.getHeaders())
        .thenReturn(RequestHeaders.init().set(HttpHeaders.ETAG, ETAG).toHeaders());
    when(stringArrayResponse.getStatusCode()).thenReturn(HttpStatus.NOT_MODIFIED);

    ServiceResponse<List<String>> serviceResponse =
        service.getPermissionStrings(userId, ETAG);

    assertEquals(ETAG, serviceResponse.getETag());
    assertNull(serviceResponse.getBody());
    assertFalse(serviceResponse.isModified());
  }

  @Test
  public void testFindUsersByIds_withResults() {
    prepareTestsForUsersSearch();

    Set<UUID> ids = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));

    List<UserDto> usersList = Arrays.asList(new UserDto(), new UserDto());
    Page<UserDto> page = mock(Page.class);
    when(page.getContent()).thenReturn(usersList);

    doReturn(page).when(service).getPage(eq(SEARCH), eq(Collections.emptyMap()), anyMap());

    Collection<UserDto> result = service.findUsersByIds(ids);

    assertNotNull(result);
    Assertions.assertEquals(usersList.size(), result.size());
    Assertions.assertTrue(result.containsAll(usersList));
  }

  @Test
  public void testFindUsersByIds_nullPage() {
    prepareTestsForUsersSearch();

    Set<UUID> ids = Collections.singleton(UUID.randomUUID());

    doReturn(null).when(service).getPage(eq(SEARCH), eq(Collections.emptyMap()), anyMap());

    Collection<UserDto> result = service.findUsersByIds(ids);

    assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void testFindUsersByIds_emptyResults() {
    prepareTestsForUsersSearch();

    Set<UUID> ids = Collections.singleton(UUID.randomUUID());

    Page<UserDto> page = mock(Page.class);
    when(page.getContent()).thenReturn(Collections.emptyList());

    doReturn(page).when(service).getPage(eq(SEARCH), eq(Collections.emptyMap()), anyMap());

    Collection<UserDto> result = service.findUsersByIds(ids);

    assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void testFindUsersByIds_partialResults() {
    prepareTestsForUsersSearch();

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    UserDto user = new UserDto();
    user.setId(id1);

    Set<UUID> ids = new HashSet<>(Arrays.asList(id1, id2));

    Page<UserDto> page = mock(Page.class);
    when(page.getContent()).thenReturn(Collections.singletonList(user));
    doReturn(page).when(service).getPage(eq(SEARCH), eq(Collections.emptyMap()), anyMap());

    Collection<UserDto> result = service.findUsersByIds(ids);

    assertNotNull(result);
    Assertions.assertEquals(1, result.size());
    Assertions.assertTrue(result.contains(user));
  }

  private void prepareTestsForUsersSearch() {
    service = spy(new UserReferenceDataService());
    skipAfter = true;
  }
}
