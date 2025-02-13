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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.service.BaseCommunicationService;
import org.springframework.http.HttpMethod;

@RunWith(MockitoJUnitRunner.class)
public class ProgramReferenceDataServiceTest extends BaseReferenceDataServiceTest<ProgramDto> {

  private ProgramReferenceDataService service;

  @Override
  protected BaseCommunicationService<ProgramDto> getService() {
    return new ProgramReferenceDataService();
  }

  @Override
  protected ProgramDto generateInstance() {
    return ProgramDto.builder().id(UUID.randomUUID()).code(RandomStringUtils.randomAlphabetic(3))
        .build();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    service = (ProgramReferenceDataService) prepareService();
  }

  @Test
  public void findByIdsShouldReturnMatchingPrograms() {
    final ProgramDto program = generateInstance();
    mockArrayResponse(new ProgramDto[] {program});

    final List<ProgramDto> response = service.findByIds(singletonList(program.getId()));

    assertThat(response, hasSize(1));
    assertThat(response, hasItem(program));

    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        eq(ProgramDto[].class));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?id=" + program.getId().toString(),
        uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void findByCodeShouldReturnMatchingProgram() {
    final ProgramDto program = generateInstance();
    mockArrayResponse(new ProgramDto[] {program});

    final Optional<ProgramDto> response = service.findByCode(program.getCode());

    assertTrue(response.isPresent());

    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        eq(ProgramDto[].class));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?code=" + program.getCode(), uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }
}
