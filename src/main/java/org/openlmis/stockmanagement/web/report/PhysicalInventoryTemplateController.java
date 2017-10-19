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

package org.openlmis.stockmanagement.web.report;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_IO;

import org.apache.commons.io.IOUtils;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.JasperTemplateService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;

@Controller
@Transactional
@RequestMapping("/api/physicalInventoryTemplates")
public class PhysicalInventoryTemplateController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PhysicalInventoryTemplateController.class);

  private static final String PRINT_PI = "Print PI";
  private static final String DESCRIPTION_PI = "Template to print Physical Inventory";
  private static final String CONSISTENCY_REPORT = "Consistency Report";

  @Autowired
  private JasperTemplateService templateService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Add Physical Inventory report templates with ".jrxml" format(extension) to database, remove
   * older one if already exists.
   *
   * @param file File in ".jrxml" format to add or upload.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.OK)
  public void saveTemplate(@RequestPart("file") MultipartFile file) {

    LOGGER.debug("Checking right to create Physical Inventory template");
    permissionService.canManageSystemSettings();

    JasperTemplate template =
        new JasperTemplate(PRINT_PI, null, CONSISTENCY_REPORT, DESCRIPTION_PI);
    templateService.validateFileAndSaveTemplate(template, file);
  }

  /**
   * Download report template with ".jrxml" format(extension) for Physical Inventory from database.
   *
   * @param response HttpServletResponse object.
   */
  @GetMapping
  @ResponseBody
  public void downloadXmlTemplate(HttpServletResponse response)
      throws IOException {
    LOGGER.debug("Checking right to view Physical Inventory template");
    permissionService.canManageSystemSettings();
    JasperTemplate piPrintTemplate = templateService.getByName(PRINT_PI);
    if (piPrintTemplate == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          "Physical Inventory template does not exist.");
    } else {
      response.setContentType("application/xml");
      response.addHeader("Content-Disposition", "attachment; filename=piPrint" + ".jrxml");

      File file = templateService.convertJasperToXml(piPrintTemplate);

      try (InputStream fis = new FileInputStream(file);
           InputStream bis = new BufferedInputStream(fis)) {

        IOUtils.copy(bis, response.getOutputStream());
        response.flushBuffer();
      } catch (IOException ex) {
        throw new ValidationMessageException(ex, new Message(ERROR_IO, ex.getMessage()));
      }
    }
  }
}
