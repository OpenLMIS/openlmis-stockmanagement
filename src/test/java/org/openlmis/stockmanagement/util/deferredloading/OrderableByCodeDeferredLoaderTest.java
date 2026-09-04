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

package org.openlmis.stockmanagement.util.deferredloading;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class OrderableByCodeDeferredLoaderTest {
  private static final List<String> TEST_OBJECT_KEYS = asList("TEST123", "TEST321", "TEST!@#");
  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Test
  public void shouldLoadAllOrderablesAsSingleCall() {
    when(orderableReferenceDataService.findByExactCodes(anyCollection())).then(
        invocation -> ((Collection<String>) invocation.getArgument(0)).stream()
            .map(key -> OrderableDto.builder().productCode(key).build())
            .collect(Collectors.toList()));

    final OrderableByCodeDeferredLoader loader =
        new OrderableByCodeDeferredLoader(orderableReferenceDataService);

    for (String testKey : TEST_OBJECT_KEYS) {
      loader.deferredLoad(testKey);
    }

    loader.loadDeferredObjects();

    verify(orderableReferenceDataService, only()).findByExactCodes(anyCollection());
  }

  @Test
  public void shouldThrowExceptionIfOrderableNotFound() {
    final OrderableByCodeDeferredLoader loader =
        new OrderableByCodeDeferredLoader(orderableReferenceDataService);

    loader.deferredLoad(TEST_OBJECT_KEYS.get(0));

    try {
      loader.loadDeferredObjects();
      fail("Should throw exception");
    } catch (ValidationMessageException vme) {
      assertTrue("Message should contain the key of missing Orderable",
          vme.getMessage().contains(TEST_OBJECT_KEYS.get(0)));
    } catch (Exception e) {
      fail("Should throw ValidationMessageException");
    }
  }
}
