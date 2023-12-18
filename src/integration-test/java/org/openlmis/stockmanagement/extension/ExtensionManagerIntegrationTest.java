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

package org.openlmis.stockmanagement.extension;

import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.extension.point.AdjustmentReasonValidator;
import org.openlmis.stockmanagement.extension.point.ExtensionPointId;
import org.openlmis.stockmanagement.validators.DefaultAdjustmentReasonValidator;
import org.openlmis.stockmanagement.validators.StockEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-run")
@SuppressWarnings("PMD.UnusedLocalVariable")
public class ExtensionManagerIntegrationTest {

  private static final String invalidPointId = "InvalidExtensionPoint";
  private static final String invalidExtensionId = "InvalidExtension";
  private static final String extensionId = "DefaultAdjustmentReasonValidator";

  private HashMap<String, String> extensions;

  @Autowired
  private ExtensionManager extensionManager;

  /**
   * Prepare the test environment - add extensions for test purposes.
   */
  @Before
  public void setUp() {
    extensions = new HashMap<>();
    extensions.put(ExtensionPointId.ADJUSTMENT_REASON_POINT_ID, extensionId);
    extensions.put(invalidPointId, invalidExtensionId);
    extensionManager.setExtensions(extensions);
  }

  @Test
  public void testShouldReturnExtensionWithGivenIdAndClass() {
    StockEventValidator orderQuantity = (StockEventValidator) extensionManager
        .getExtension(ExtensionPointId.ADJUSTMENT_REASON_POINT_ID, AdjustmentReasonValidator.class);
    Assert.assertEquals(orderQuantity.getClass(), DefaultAdjustmentReasonValidator.class);
  }

  @Test
  public void testShouldReturnExtensionWithGivenClassIfMappingDoesNotExist() {
    StockEventValidator orderQuantity = (StockEventValidator) extensionManager
        .getExtension("test", AdjustmentReasonValidator.class);
    Assert.assertEquals(orderQuantity.getClass(), DefaultAdjustmentReasonValidator.class);
  }

  @Test(expected = ExtensionException.class)
  public void testShouldNotReturnExtensionByPointIdWhenInvalidIdAndClass() {
    StockEventValidator orderQuantity = (StockEventValidator) extensionManager
        .getExtension(invalidPointId, getClass());
  }
}
