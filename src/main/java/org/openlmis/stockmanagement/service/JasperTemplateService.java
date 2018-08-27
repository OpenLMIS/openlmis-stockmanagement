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

import static java.io.File.createTempFile;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_IO;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORTING_CREATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORTING_FILE_EMPTY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORTING_FILE_INCORRECT_TYPE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORTING_FILE_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORTING_FILE_MISSING;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.TemplateRepository;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class JasperTemplateService {

  @Autowired
  private TemplateRepository templateRepository;

  public JasperTemplate getByName(String name) {
    return templateRepository.findByName(name);
  }

  /**
   * Validate ".jrmxl" file and insert if template not exist.
   * If this name of template already exist, remove older template and insert new.
   */
  public void validateFileAndSaveTemplate(JasperTemplate template, MultipartFile file) {
    JasperTemplate templateTmp = templateRepository.findByName(template.getName());
    if (templateTmp != null) {
      templateRepository.removeAndFlush(templateTmp);
    }

    validateFileAndSetData(template, file);
    templateRepository.save(template);
  }

  /**
   * Convert template from ".jasper" format in database to ".jrxml"(extension) format.
   */
  public File convertJasperToXml(JasperTemplate template) {
    try (InputStream inputStream = new ByteArrayInputStream(template.getData());
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      JasperCompileManager.writeReportToXmlStream(inputStream, outputStream);
      File xmlReport = createTempFile(template.getName(), ".jrxml");
      writeByteArrayToFile(xmlReport, outputStream.toByteArray());
      return xmlReport;
    } catch (JRException | IOException ex) {
      throw new ValidationMessageException(ex, ERROR_REPORTING_CREATION);
    }
  }

  /**
   * Validate ".jrxml" report file. If it is valid, create template parameters.
   */
  private void validateFileAndSetData(JasperTemplate template, MultipartFile file) {
    if (file == null) {
      throw new ValidationMessageException(ERROR_REPORTING_FILE_MISSING);
    }
    if (!file.getOriginalFilename().endsWith(".jrxml")) {
      throw new ValidationMessageException(ERROR_REPORTING_FILE_INCORRECT_TYPE);
    }
    if (file.isEmpty()) {
      throw new ValidationMessageException(ERROR_REPORTING_FILE_EMPTY);
    }

    try {
      createTemplate(template, file.getInputStream());
    } catch (IOException ex) {
      throw new ValidationMessageException(ex, new Message(ERROR_IO, ex.getMessage()));
    }
  }

  /**
   * Save report file as ".jasper" in byte array in Template class.
   * If report is not valid throw exception.
   *
   * @param template The template to insert parameters to
   * @param inputStream input stream of the file
   */
  private void createTemplate(JasperTemplate template, InputStream inputStream) {
    try {
      JasperReport report = JasperCompileManager.compileReport(inputStream);

      String reportType = report.getProperty("reportType");
      if (reportType != null) {
        template.setType(reportType);
      }

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bos);
      out.writeObject(report);
      template.setData(bos.toByteArray());
    } catch (JRException ex) {
      throw new ValidationMessageException(ex,
          new Message(ERROR_REPORTING_FILE_INVALID, ex.getMessage()));
    } catch (IOException ex) {
      throw new ValidationMessageException(ex, new Message(ERROR_IO, ex.getMessage()));
    }
  }

}
