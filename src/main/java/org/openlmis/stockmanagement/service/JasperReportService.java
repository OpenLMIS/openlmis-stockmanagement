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

import static java.util.Collections.singletonList;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_CLASS_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_GENERATE_REPORT_FAILED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_IO;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REPORT_ID_NOT_FOUND;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.exception.JasperReportViewException;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.service.report.ReportService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JasperReportService {

  static final String CARD_REPORT_URL = "/jasperTemplates/stockCard.jrxml";
  static final String CARD_SUMMARY_REPORT_URL = "/jasperTemplates/stockCardSummary.jrxml";
  static final String STOCK_EVENT_REPORT_URL = "/jasperTemplates/stockEvent.jrxml";
  static final String PI_LINES_REPORT_URL = "/jasperTemplates/physicalinventoryLines.jrxml";

  private static final String PARAM_DATASOURCE = "datasource";
  // Net content used when an orderable has none: 1 dose per pack, so the packs
  // conversion stays a safe division and falls back to showing the raw value.
  private static final long DEFAULT_NET_CONTENT = 1L;
  private static final String PARAM_DATE_FORMAT = "dateFormat";
  private static final String PARAM_DECIMAL_FORMAT = "decimalFormat";
  
  private final StockCardService stockCardService;
  private final StockCardSummariesService stockCardSummariesService;
  private final ReportService reportService;
  private final DataSource replicationDataSource;

  @Value("${dateFormat}")
  private String dateFormat;

  @Value("${dateTimeFormat}")
  private String dateTimeFormat;

  @Value("${time.zoneId}")
  private String timeZoneId;

  @Value("${groupingSeparator}")
  private String groupingSeparator;

  @Value("${groupingSize}")
  private String groupingSize;

  /**
   * Generate stock card report in PDF format.
   *
   * @param stockCardId stock card id
   * @param lang        the lang
   * @param showInDoses whether values should be shown in doses (true) or packs (false)
   * @return generated stock card report.
   */
  public byte[] generateStockCardReport(UUID stockCardId, String lang, Boolean showInDoses) {
    StockCardDto stockCardDto = stockCardService.findStockCardById(stockCardId);
    if (stockCardDto == null) {
      throw new ResourceNotFoundException(new Message(ERROR_REPORT_ID_NOT_FOUND));
    }

    Collections.reverse(stockCardDto.getLineItems());
    Map<String, Object> params = new HashMap<>();
    params.put(PARAM_DATASOURCE, singletonList(stockCardDto));
    params.put("hasLot", stockCardDto.hasLot());
    params.put("showInDoses", showInDoses);
    params.put("orderableNetContent", guardedNetContent(stockCardDto.getOrderable()));
    params.put(PARAM_DATE_FORMAT, dateFormat);
    params.put(PARAM_DECIMAL_FORMAT, createDecimalFormat());
    params.put("lang", lang);

    JasperReport compiledReport = compileReportFromTemplateUrl(CARD_REPORT_URL);
    return reportService.fillAndExportReport("stockCard",
        serializeReport(compiledReport), params);
  }

  /**
   * Generate stock card summary report in PDF format.
   *
   * @param program  program id
   * @param facility facility id
   * @param lang     the lang
   * @param showInDoses whether values should be shown in doses (true) or packs (false)
   * @return generated stock card summary report.
   */
  public byte[] generateStockCardSummariesReport(UUID program, UUID facility, String lang,
      Boolean showInDoses) {
    List<StockCardDto> cards = stockCardSummariesService
        .findStockCards(program, facility);
    StockCardDto firstCard = cards.get(0);
    Map<String, Object> params = new HashMap<>();
    params.put("stockCardSummaries", cards);
    params.put("showInDoses", showInDoses);

    params.put("program", firstCard.getProgram());
    params.put("facility", firstCard.getFacility());
    //right now, each report can only be about one program, one facility
    //in the future we may want to support one report for multiple programs
    params.put("showProgram", getCount(cards, card -> card.getProgram().getId().toString()) > 1);
    params.put("showFacility", getCount(cards, card -> card.getFacility().getId().toString()) > 1);
    params.put("showLot", cards.stream().anyMatch(card -> card.getLotId() != null));
    params.put(PARAM_DATE_FORMAT, dateFormat);
    params.put("dateTimeFormat", dateTimeFormat);
    params.put(PARAM_DECIMAL_FORMAT, createDecimalFormat());
    params.put("lang", lang);

    JasperReport compiledReport = compileReportFromTemplateUrl(CARD_SUMMARY_REPORT_URL);
    return reportService.fillAndExportReport("stockCardSummary",
        serializeReport(compiledReport), params);
  }

  /**
   * Generate stock event report byte [ ].
   *
   * @param stockEventId the stock event id
   * @param lang         the lang
   * @param showInDoses  whether quantities should be presented in doses (otherwise in packs)
   * @return the byte [ ]
   */
  public byte[] generateStockEventReport(UUID stockEventId, String lang, Boolean showInDoses) {
    Map<String, Object> params = new HashMap<>();

    params.put("stockEventId", stockEventId);
    params.put(PARAM_DATE_FORMAT, dateFormat);
    params.put("dateTimeFormat", dateTimeFormat);
    params.put("timeZoneId", timeZoneId);
    params.put(PARAM_DECIMAL_FORMAT, createDecimalFormat());
    params.put("lang", lang);
    params.put("showInDoses", showInDoses);

    JasperReport compiledReport = compileReportFromTemplateUrl(STOCK_EVENT_REPORT_URL);
    return reportService.fillAndExportReport("stockEvent",
        serializeReport(compiledReport), params);
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
    return fillAndExportReport(getReportFromTemplateData(jasperTemplate), params);
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

  /**
   * Gets compiled physical inventory line subreport bytes.
   *
   * @return the compiled physical inventory line subreport bytes
   */
  public byte[] getCompiledPhysicalInventoryLineSubreportBytes() {
    return serializeReport(compileReportFromTemplateUrl(PI_LINES_REPORT_URL));
  }

  private long getCount(List<StockCardDto> stockCards, Function<StockCardDto, String> mapper) {
    return stockCards.stream().map(mapper).distinct().count();
  }

  /**
   * Net content used to convert doses to packs, guarded so it is always a safe divisor.
   * Computed here (not in the template) because the page header needs the value as a
   * parameter, which - unlike a dataset variable - is available when the header is rendered.
   *
   * @param orderable the report's orderable (may be null)
   * @return the orderable's net content, or 1 when it is missing or not positive
   */
  private long guardedNetContent(OrderableDto orderable) {
    if (orderable == null || orderable.getNetContent() == null || orderable.getNetContent() <= 0) {
      return DEFAULT_NET_CONTENT;
    }
    return orderable.getNetContent();
  }

  private byte[] serializeReport(JasperReport report) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(report);
      return bos.toByteArray();
    } catch (IOException ex) {
      throw new JasperReportViewException(new Message(ERROR_IO, ex.getMessage()), ex);
    }
  }

  byte[] fillAndExportReport(JasperReport compiledReport, Map<String, Object> params) {

    byte[] bytes;

    try {
      JasperPrint jasperPrint;
      if (params.containsKey(PARAM_DATASOURCE)) {
        jasperPrint = JasperFillManager.fillReport(compiledReport, params,
            new JRBeanCollectionDataSource((List<StockCardDto>) params.get(PARAM_DATASOURCE)));
      } else if (params.containsKey("stockCardSummaries")) {
        jasperPrint = JasperFillManager.fillReport(compiledReport, params, 
            new JREmptyDataSource());
      } else {
        try (Connection connection = replicationDataSource.getConnection()) {
          jasperPrint = JasperFillManager.fillReport(compiledReport, params,
              connection);
        }
      }

      bytes = JasperExportManager.exportReportToPdf(jasperPrint);
    } catch (Exception e) {
      throw new JasperReportViewException(ERROR_GENERATE_REPORT_FAILED, e);
    }

    return bytes;
  }

  JasperReport compileReportFromTemplateUrl(String templateUrl) {
    try (InputStream inputStream = getClass().getResourceAsStream(templateUrl)) {

      return JasperCompileManager.compileReport(inputStream);
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
  JasperReport getReportFromTemplateData(JasperTemplate jasperTemplate) {

    try (ObjectInputStream inputStream =
             new ObjectInputStream(new ByteArrayInputStream(jasperTemplate.getData()))) {

      return (JasperReport) inputStream.readObject();
    } catch (IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    } catch (ClassNotFoundException ex) {
      throw new JasperReportViewException(
          new Message(ERROR_CLASS_NOT_FOUND, JasperReport.class.getName()), ex);
    }
  }

  private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.parseInt(groupingSize));
    return decimalFormat;
  }
}
