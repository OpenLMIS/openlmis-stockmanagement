/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2020 VillageReach
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

import java.io.ByteArrayOutputStream;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

public class JasperCsvExporter implements JasperExporter {

  private JasperPrint jasperPrint;
  
  JasperCsvExporter(JasperPrint jasperPrint) {
    this.jasperPrint = jasperPrint;
  }

  @Override
  public byte[] exportReport() throws JRException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JRCsvExporter exporter = new JRCsvExporter();
    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
    exporter.setExporterOutput(new SimpleWriterExporterOutput(baos));
    exporter.exportReport();
    return baos.toByteArray();
  }
}
