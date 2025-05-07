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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.service.referencedata.BaseReferenceDataServiceTest;
import org.openlmis.stockmanagement.service.referencedata.SupervisingUsersReferenceDataService;
import org.openlmis.stockmanagement.testutils.ObjectGenerator;
import org.springframework.http.HttpMethod;

public class SupervisingUsersReferenceDataServiceTest
    extends BaseReferenceDataServiceTest<UserDto> {

  private SupervisingUsersReferenceDataService service;

  private UUID supervisoryNode = UUID.randomUUID();
  private UUID right = UUID.randomUUID();
  private UUID program = UUID.randomUUID();
  private UserDto userDto = generateInstance();

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    service = (SupervisingUsersReferenceDataService) prepareService();
  }

  @Test
  public void testFindAll() {
    mockArrayResponse(new UserDto[]{userDto});

    Collection<UserDto> userDtos = service.findAll(supervisoryNode, right, program);

    assertThat(userDtos, hasSize(1));
    assertThat(userDtos, hasItem(userDto));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET),
        entityCaptor.capture(), eq(UserDto[].class)
    );

    URI uri = uriCaptor.getValue();
    String url = serviceUrl + "/api/supervisoryNodes/" + supervisoryNode + "/supervisingUsers";

    assertThat(uri.toString(), startsWith(url));
    assertThat(uri.toString(), containsString("rightId=" + right));
    assertThat(uri.toString(), containsString("programId=" + program));

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Override
  protected BaseCommunicationService<UserDto> getService() {
    return new SupervisingUsersReferenceDataService();
  }

  @Override
  protected UserDto generateInstance() {
    return ObjectGenerator.of(UserDto.class);
  }
}
