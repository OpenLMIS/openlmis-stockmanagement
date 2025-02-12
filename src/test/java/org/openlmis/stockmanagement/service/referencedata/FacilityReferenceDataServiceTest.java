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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.service.BaseCommunicationService;
import org.openlmis.stockmanagement.util.DynamicPageTypeReference;
import org.springframework.http.HttpMethod;

@RunWith(MockitoJUnitRunner.class)
public class FacilityReferenceDataServiceTest extends BaseReferenceDataServiceTest<FacilityDto> {
  private FacilityReferenceDataService service;

  @Override
  protected BaseCommunicationService<FacilityDto> getService() {
    return new FacilityReferenceDataService();
  }

  @Override
  protected FacilityDto generateInstance() {
    return FacilityDto.builder().id(UUID.randomUUID()).code(RandomStringUtils.randomAlphabetic(3))
        .build();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    service = (FacilityReferenceDataService) prepareService();
  }

  @Test
  public void findByIdsShouldReturnMatchingFacilitys() {
    final FacilityDto facility = mockPageResponseEntityAndGetDto();

    final Map<UUID, FacilityDto> response = service.findByIds(singletonList(facility.getId()));

    assertEquals(1, response.size());
    assertTrue(response.containsValue(facility));

    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(FacilityDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?id=" + facility.getId().toString(),
        uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void findByCodeShouldReturnMatchingFacility() {
    final FacilityDto facility = mockPageResponseEntityAndGetDto();

    final Optional<FacilityDto> response = service.findByCode(facility.getCode());

    assertTrue(response.isPresent());

    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(FacilityDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?code=" + facility.getCode(), uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }
}
