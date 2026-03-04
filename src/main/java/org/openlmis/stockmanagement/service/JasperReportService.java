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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Function;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.OrientationEnum;
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
  static final String PI_LINES_REPORT_URL = "/jasperTemplates/physicalinventoryLines.jrxml";

  private static final String PARAM_DATASOURCE = "datasource";
  private static final String configPath = "/config/reports/";
  
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
  public byte[] generateStockCardReport(UUID stockCardId, String lang) {
    StockCardDto stockCardDto = stockCardService.findStockCardById(stockCardId);
    if (stockCardDto == null) {
      throw new ResourceNotFoundException(new Message(ERROR_REPORT_ID_NOT_FOUND));
    }

    Collections.reverse(stockCardDto.getLineItems());
    Map<String, Object> params = new HashMap<>();
    params.put(PARAM_DATASOURCE, singletonList(stockCardDto));
    params.put("hasLot", stockCardDto.hasLot());
    params.put("dateFormat", dateFormat);
    params.put("decimalFormat", createDecimalFormat());

    JasperReport compiledReport = compileReportFromTemplateUrl(CARD_SUMMARY_REPORT_URL);
    try {
      params.putAll(getLocaleBundleParameters(compiledReport, lang));
      params.putAll(getMapSubreportGlobalHeaderParameters(compiledReport));
    } catch (JRException | IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    }

    return fillAndExportReport(compileReportFromTemplateUrl(CARD_REPORT_URL), params);
  }

  /**
   * Generate stock card summary report in PDF format.
   *
   * @param program  program id
   * @param facility facility id
   * @return generated stock card summary report.
   */
  public byte[] generateStockCardSummariesReport(UUID program, UUID facility, String lang) {
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

    JasperReport compiledReport = compileReportFromTemplateUrl(CARD_SUMMARY_REPORT_URL);
    try {
      params.putAll(getLocaleBundleParameters(compiledReport, lang));
      params.putAll(getMapSubreportGlobalHeaderParameters(compiledReport));
    } catch (JRException | IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    }

    return fillAndExportReport(compiledReport, params);
  }

  /**
   * Generate report with custom header byte [ ].
   *
   * @param jasperTemplate the jasper template
   * @param params         the params
   * @param lang           the lang
   * @return the byte [ ]
   */
  public byte[] generateReportWithCustomHeader(JasperTemplate jasperTemplate,
      Map<String, Object> params, String lang) {
    JasperReport compiledReport = getReportFromTemplateData(jasperTemplate);
    try {
      params.putAll(getLocaleBundleParameters(compiledReport, lang));
      params.putAll(getMapSubreportGlobalHeaderParameters(compiledReport));
    } catch (JRException | IOException ex) {
      throw new JasperReportViewException(new Message((ERROR_IO), ex.getMessage()), ex);
    }
    return fillAndExportReport(compiledReport, params);
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

  private long getCount(List<StockCardDto> stockCards, Function<StockCardDto, String> mapper) {
    return stockCards.stream().map(mapper).distinct().count();
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

  /**
   * Gets locale for translation resource bundle parameters.
   *
   * @param userLocaleString the user locale string
   * @return the locale bundle parameters
   * @throws MalformedURLException the malformed url exception
   */
  public Map<String, Object> getLocaleBundleParameters(JasperReport parentReport,
                                                       String userLocaleString)
      throws MalformedURLException {
    String resourceBundleName = parentReport != null ? parentReport.getResourceBundle() : null;
    if (resourceBundleName == null || resourceBundleName.trim().isEmpty()) {
      return Collections.emptyMap();
    }

    Locale userLocale;
    try {
      userLocale = new Locale.Builder().setLanguageTag(userLocaleString).build();
    } catch (Exception e) {
      userLocale = Locale.ENGLISH;
    }

    Map<String, Object> parameters = new HashMap<>();
    ResourceBundle bundle = loadResourceBundle(userLocale);
    
    if (bundle != null) {
      parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, bundle);
      parameters.put(JRParameter.REPORT_LOCALE, userLocale);
    }
    
    return parameters;
  }

  private ResourceBundle loadResourceBundle(Locale locale) {
    File resourceBundleDir = new File(configPath + "resourceBundles");

    // Attempt to load from the config
    if (resourceBundleDir.exists() && resourceBundleDir.isDirectory()) {
      try {
        URL[] urls = {resourceBundleDir.toURI().toURL()};
        try (URLClassLoader externalLoader = new URLClassLoader(urls)) {
          return ResourceBundle.getBundle("report_translations", locale, externalLoader);
        }
      } catch (IOException | MissingResourceException e) {
        resourceBundleDir = null;
      }
    }

    // Fallback to the internal Classpath
    try {
      return ResourceBundle.getBundle("report_translations", locale);
    } catch (MissingResourceException e) {
      return null;
    }
  }

  /**
   * Gets map subreport global header parameters.
   *
   * @param parentReport the parent report
   * @return the map subreport global header parameters
   * @throws JRException the jr exception
   * @throws IOException the io exception
   */
  public Map<String, Object> getMapSubreportGlobalHeaderParameters(JasperReport parentReport)
      throws JRException, IOException {
    // validate if report requires header or not
    boolean needsHeader = parentReport != null && parentReport.getParameters() != null
            && Arrays.stream(parentReport.getParameters())
        .anyMatch(param -> "headerTemplate".equals(param.getName()));
    if (!needsHeader) {
      return Collections.emptyMap();
    }

    File configDir = new File(configPath);
    if (!configDir.exists() || !configDir.isDirectory()) {
      // config directory does not exist
      return Collections.emptyMap();
    }

    String headerName;
    if (OrientationEnum.LANDSCAPE.equals(parentReport.getOrientationValue())) {
      headerName = "GlobalHeaderLandscape";
    } else if (OrientationEnum.PORTRAIT.equals(parentReport.getOrientationValue())) {
      headerName = "GlobalHeaderPortrait";
    } else {
      // no orientation recognized
      return Collections.emptyMap();
    }

    Map<String, Object> parameters = new HashMap<>();
    File headerFile = new File(configPath + headerName + ".jrxml");
    if (headerFile.exists()) {
      try (InputStream is = Files.newInputStream(headerFile.toPath())) {
        JasperReport globalHeader = JasperCompileManager.compileReport(is);
        parameters.put("headerTemplate", globalHeader);
      } catch (JRException | IOException e) {
        throw new JasperReportViewException(new Message(ERROR_GENERATE_REPORT_FAILED), e);
      }
    } else {
      return Collections.emptyMap();
    }

    parameters.putAll(injectDynamicHeaderParams());
    return parameters;
  }

  /**
   * Inject dynamic header params map.
   *
   * @return the map
   * @throws IOException the io exception
   */
  private Map<String, Object> injectDynamicHeaderParams() throws IOException {
    Map<String, Object> parameters = new HashMap<>();
    File configFile = new File(configPath + "header_config.properties");

    if (configFile.exists()) {
      Properties dynamicProps = new Properties();
      try (InputStream is = Files.newInputStream(configFile.toPath())) {
        dynamicProps.load(is);
      }

      for (String key : dynamicProps.stringPropertyNames()) {
        String value = dynamicProps.getProperty(key);

        if (key.endsWith("Image")) {
          File imageFile = new File(configPath + value);
          if (imageFile.exists()) {
            parameters.put(key, imageFile.getAbsolutePath());
          }
        } else {
          parameters.put(key, value);
        }
      }
    }
    return parameters;
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
