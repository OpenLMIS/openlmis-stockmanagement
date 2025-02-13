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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class OrderableDeferredLoaderTest {
  private static final List<UUID> TEST_OBJECT_KEYS =
      asList(UUID.fromString("9dc573b1-c6fc-49f4-a9cf-5e7f344cbb75"),
          UUID.fromString("71c5472d-9fd0-4ad1-9fa6-ae57324ff39a"),
          UUID.fromString("323b264f-c1c1-4fb4-b03c-6b7c9edc4356"));
  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Test
  public void shouldLoadAllLotsAsSingleCall() {
    final OrderableDeferredLoader loader =
        new OrderableDeferredLoader(orderableReferenceDataService);

    for (UUID testKey : TEST_OBJECT_KEYS) {
      loader.deferredLoad(testKey);
    }

    loader.loadDeferredObjects();

    verify(orderableReferenceDataService, only()).findByIds(anyCollection());
  }
}
