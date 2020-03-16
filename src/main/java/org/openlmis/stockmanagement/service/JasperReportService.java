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
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_CLASS_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_GENERATE_REPORT_FAILED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_IO;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_JASPER_FILE_CREATION;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORT_FORMAT_UNKNOWN;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORT_ID_NOT_FOUND;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JasperReportService {

  static final String CARD_REPORT_URL = "/jasperTemplates/stockCard.jrxml";
  static final String CARD_SUMMARY_REPORT_URL = "/jasperTemplates/stockCardSummary.jrxml";
  private static final String PI_LINES_REPORT_URL = "/jasperTemplates/physicalinventoryLines.jrxml";

  @Autowired
  private StockCardService stockCardService;

  @Autowired
  private StockCardSummariesService stockCardSummariesService;

  @Autowired
  private DataSource replicationDataSource;

  @Value("${dateFormat}")
  private String dateFormat;

  @Value("${dateTimeFormat}")
  private String dateTimeFormat;

  @Value("${groupingSeparator}")
  private String groupingSeparator;

  @Value("${groupingSize}")
  private String groupingSize;

  /**
   * Generate stock card report in PDF format.
   *
   * @param stockCardId stock card id
   * @return generated stock card report.
   */
  public byte[] generateStockCardReport(UUID stockCardId) {
    StockCardDto stockCardDto = stockCardService.findStockCardById(stockCardId);
    if (stockCardDto == null) {
      throw new ResourceNotFoundException(new Message(ERROR_REPORT_ID_NOT_FOUND));
    }

    Collections.reverse(stockCardDto.getLineItems());
    Map<String, Object> params = new HashMap<>();
    params.put("datasource", singletonList(stockCardDto));
    params.put("hasLot", stockCardDto.hasLot());
    params.put("dateFormat", dateFormat);
    params.put("decimalFormat", createDecimalFormat());

    return fillAndExportReport(compileReportAndGetUrl(CARD_REPORT_URL), params);
  }

  /**
   * Generate stock card summary report in PDF format.
   *
   * @param program  program id
   * @param facility facility id
   * @return generated stock card summary report.
   */
  public byte[] generateStockCardSummariesReport(UUID program, UUID facility) {
    List<StockCardDto> cards = stockCardSummariesService
        .findStockCards(program, facility);
    StockCardDto firstCard = cards.get(0);
    Map<String, Object> params = new HashMap<>();
    params.put("stockCardSummaries", cards);

    params.put("program", firstCard.getProgram());
    params.put("facility", firstCard.getFacility());
    //right now, each report can only be about one program, one facility
    //in the future we may want to support one report for multiple programs
    params.put("showProgram", getCount(cards, card -> card.getProgram().getId().toString()) > 1);
    params.put("showFacility", getCount(cards, card -> card.getFacility().getId().toString()) > 1);
    params.put("showLot", cards.stream().anyMatch(card -> card.getLotId() != null));
    params.put("dateFormat", dateFormat);
    params.put("dateTimeFormat", dateTimeFormat);
    params.put("decimalFormat", createDecimalFormat());

    return fillAndExportReport(compileReportAndGetUrl(CARD_SUMMARY_REPORT_URL), params);
  }

  /**
   * Generate a report based on the Jasper template.
   * Create compiled report (".jasper" file) from bytes from Template entity, and get URL.
   * Using compiled report URL to fill in data and export to desired format.
   *
   * @param jasperTemplate template that will be used to generate a report
   * @param params  map of parameters
   * @return data of generated report
   */
  public byte[] generateReport(JasperTemplate jasperTemplate, Map<String, Object> params) {
    return fillAndExportReport(getReportUrlForReportData(jasperTemplate), params);
  }

  /**
   * Creates PI line sub-report.
   * */
  public JasperDesign createCustomizedPhysicalInventoryLineSubreport() {
    try (InputStream inputStream = getClass().getResourceAsStream(PI_LINES_REPORT_URL)) {
      return JRXmlLoader.load(inputStream);
    } catch (IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    } catch (JRException ex) {
      throw new JasperReportViewException(new Message(ERROR_GENERATE_REPORT_FAILED), ex);
    }
  }

  private long getCount(List<StockCardDto> stockCards, Function<StockCardDto, String> mapper) {
    return stockCards.stream().map(mapper).distinct().count();
  }

  byte[] fillAndExportReport(String compiledTemplateUrl, Map<String, Object> params) {

    byte[] bytes;

    try {
      JasperPrint jasperPrint = JasperFillManager.fillReport(
          compiledTemplateUrl,
          params,
          replicationDataSource.getConnection());

      JasperExporter exporter;
      String format = (String) params.get("format");
      if ("pdf".equals(format)) {
        bytes = JasperExportManager.exportReportToPdf(jasperPrint);
      } else if ("csv".equals(format)) {
        exporter = new JasperCsvExporter(jasperPrint);
        bytes = exporter.exportReport();
      } else if ("xls".equals(format)) {
        exporter = new JasperXlsExporter(jasperPrint);
        bytes = exporter.exportReport();
      } else if ("html".equals(format)) {
        exporter = new JasperHtmlExporter(jasperPrint);
        bytes = exporter.exportReport();
      } else {
        throw new IllegalArgumentException(format);
      }
    } catch (IllegalArgumentException iae) {
      throw new JasperReportViewException(ERROR_REPORT_FORMAT_UNKNOWN + iae.getMessage(),
          iae);
    } catch (Exception e) {
      throw new JasperReportViewException(ERROR_GENERATE_REPORT_FAILED, e);
    }

    return bytes;
  }

  private String compileReportAndGetUrl(String templateUrl) {
    try (InputStream inputStream = getClass().getResourceAsStream(templateUrl)) {
      JasperReport report = JasperCompileManager.compileReport(inputStream);

      return saveAndGetUrl(report, "report_temp");
    } catch (IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    } catch (JRException ex) {
      throw new JasperReportViewException(new Message(ERROR_GENERATE_REPORT_FAILED), ex);
    }
  }

  /**
   * Create ".jasper" file with byte array from Template.
   *
   * @return Url to ".jasper" file.
   */
  private String getReportUrlForReportData(JasperTemplate jasperTemplate) {

    try (ObjectInputStream inputStream =
             new ObjectInputStream(new ByteArrayInputStream(jasperTemplate.getData()))) {
      JasperReport jasperReport = (JasperReport) inputStream.readObject();

      return saveAndGetUrl(jasperReport, jasperTemplate.getName() + "_temp");
    } catch (IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    } catch (ClassNotFoundException ex) {
      throw new JasperReportViewException(
          new Message(ERROR_CLASS_NOT_FOUND, JasperReport.class.getName()), ex);
    }
  }

  private String saveAndGetUrl(JasperReport report, String templateName) throws IOException {
    File reportTempFile;
    try {
      reportTempFile = createTempFile(templateName, ".jasper");
    } catch (IOException ex) {
      throw new JasperReportViewException(ERROR_JASPER_FILE_CREATION, ex);
    }

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos)) {

      out.writeObject(report);
      writeByteArrayToFile(reportTempFile, bos.toByteArray());

      return reportTempFile.toURI().toURL().toString();
    }
  }


  private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.valueOf(groupingSize));
    return decimalFormat;
  }
}
