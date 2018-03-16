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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.util.DynamicPageTypeReference;
import org.springframework.http.HttpMethod;

public class OrderableReferenceDataServiceTest extends BaseReferenceDataServiceTest<OrderableDto> {

  private OrderableReferenceDataService service;

  @Override
  protected BaseReferenceDataService<OrderableDto> getService() {
    return new OrderableReferenceDataService();
  }

  @Override
  protected OrderableDto generateInstance() {
    return new OrderableDto();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    service = (OrderableReferenceDataService) prepareService();
  }

  @Test
  public void shouldReturnOrderables() {
    OrderableDto product = mockPageResponseEntityAndGetDto();

    List<OrderableDto> response = service.findAll();

    assertThat(response, hasSize(1));
    assertThat(response, hasItem(product));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(OrderableDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl(), uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void shouldReturnOrderablesById() {
    OrderableDto product = mockPageResponseEntityAndGetDto();

    UUID orderableId = UUID.randomUUID();
    List<OrderableDto> response = service.findByIds(Collections.singleton(orderableId));

    assertThat(response, hasSize(1));
    assertThat(response, hasItem(product));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(OrderableDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?id=" + orderableId.toString(), uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

  @Test
  public void shouldReturnEmptyListIfEmptyParamProvided() {
    checkAuth = false;
    List<OrderableDto> response = service.findByIds(Collections.emptyList());

    assertTrue(response.isEmpty());
  }
}
