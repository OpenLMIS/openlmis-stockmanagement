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

package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.extension.ExtensionManager;
import org.openlmis.stockmanagement.extension.point.AdjustmentReasonValidator;
import org.openlmis.stockmanagement.extension.point.ExtensionPointId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/extensionPoint")
public class MessageController extends BaseController {

  @Autowired
  private ExtensionManager extensionManager;

  /**
   * Returns information about extensionPoint called OrderQuantity.
   * It returns information about implementation defined for OrderQuantity
   * extension point in extensions.properties file.
   * @return information saying which class was returned as OrderQuantity implementation.
   */
  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public String extensionPoint() {
    AdjustmentReasonValidator adjustmentReasonValidator = extensionManager.getExtension(
            ExtensionPointId.ADJUSTMENT_REASON_POINT_ID, AdjustmentReasonValidator.class);
    String message = "Loaded class name: ";
    return message + adjustmentReasonValidator.getClass().getName();
  }
}