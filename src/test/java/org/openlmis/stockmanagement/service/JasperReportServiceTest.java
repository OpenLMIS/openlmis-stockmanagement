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
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.STOCK_CARD_PHYSICAL_INVENTORY;
import static org.openlmis.stockmanagement.service.JasperReportService.PI_LINES_REPORT_URL;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.stockmanagement.domain.JasperTemplate;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.physicalinventory.PhysicalInventoryLineItemAdjustment;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.i18n.MessageService;
import org.openlmis.stockmanagement.service.report.ReportService;
import org.openlmis.stockmanagement.testutils.StockCardDtoDataBuilder;
import org.openlmis.stockmanagement.util.Message;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JasperFillManager.class,JasperExportManager.class})
@SuppressWarnings("PMD.TooManyMethods")
public class JasperReportServiceTest {

  private static final String DATE_FORMAT = "dd/MM/yyyy";
  private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
  private static final String TIME_ZONE_ID = "UTC";
  private static final String GROUPING_SEPARATOR = ",";
  private static final String GROUPING_SIZE = "3";
  private static final String SHOW_IN_DOSES = "showInDoses";
  private static final String PHYSICAL_INVENTORY = "Physical Inventory";
  private static final String DAMAGE = "Damage";
  private static final String TRANSFER_IN = "Transfer In";

  private JasperReportService jasperReportService;

  @Captor
  private ArgumentCaptor<Map<String, Object>> paramsCaptor;

  @Mock
  private StockCardService stockCardService;

  @Mock
  private StockCardSummariesService stockCardSummariesService;
  
  @Mock
  private DataSource dataSource;

  @Mock
  private ReportService reportService;

  @Mock
  private MessageService messageService;

  private byte[] testReportData;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    jasperReportService = spy(new JasperReportService(stockCardService, stockCardSummariesService,
        reportService, dataSource, messageService));
    ReflectionTestUtils.setField(jasperReportService, "dateFormat", DATE_FORMAT);
    ReflectionTestUtils.setField(jasperReportService, "dateTimeFormat", DATE_TIME_FORMAT);
    ReflectionTestUtils.setField(jasperReportService, "timeZoneId", TIME_ZONE_ID);
    ReflectionTestUtils.setField(jasperReportService, "groupingSeparator", GROUPING_SEPARATOR);
    ReflectionTestUtils.setField(jasperReportService, "groupingSize", GROUPING_SIZE);
    mockStatic(JasperFillManager.class);
    mockStatic(JasperExportManager.class);
    testReportData = new byte[]{0x1};
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundExceptionWhenStockCardNotExists() {
    UUID stockCardId = UUID.randomUUID();
    when(stockCardService.findStockCardById(stockCardId)).thenReturn(null);

    jasperReportService.generateStockCardReport(stockCardId, "en", true);
  }

  @Test
  public void shouldGenerateReportWithProperParamsIfStockCardExists() {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();

    when(stockCardService.findStockCardById(stockCard.getId())).thenReturn(stockCard);

    when(reportService.fillAndExportReport(any(String.class), any(byte[].class), anyMap()))
        .thenReturn(testReportData);

    byte[] reportData =
        jasperReportService.generateStockCardReport(stockCard.getId(), "en", false);

    assertEquals(testReportData, reportData);

    verify(reportService).fillAndExportReport(any(String.class), any(byte[].class),
        paramsCaptor.capture());
    assertEquals(Boolean.FALSE, paramsCaptor.getValue().get(SHOW_IN_DOSES));
    assertEquals(1L, paramsCaptor.getValue().get("orderableNetContent"));
  }

  @Test
  public void shouldGenerateReportWithProperParamsIfStockCardSummaryExists() {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();

    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(stockCardSummariesService.findStockCards(programId, facilityId))
        .thenReturn(singletonList(stockCard));

    when(reportService.fillAndExportReport(any(String.class), any(byte[].class), anyMap()))
        .thenReturn(testReportData);

    byte[] reportData = jasperReportService.generateStockCardSummariesReport(programId,
        facilityId, "en", false);

    assertEquals(testReportData, reportData);

    verify(reportService).fillAndExportReport(any(String.class), any(byte[].class),
        paramsCaptor.capture());
    assertEquals(Boolean.FALSE, paramsCaptor.getValue().get(SHOW_IN_DOSES));
  }

  @Test
  public void shouldGenerateStockEventReportWithProperParams() {
    UUID stockEventId = UUID.randomUUID();

    when(reportService.fillAndExportReport(any(String.class), any(byte[].class), anyMap()))
        .thenReturn(testReportData);

    byte[] reportData = jasperReportService.generateStockEventReport(stockEventId, "en", true);

    assertEquals(testReportData, reportData);

    verify(reportService).fillAndExportReport(eq("stockEvent"), any(byte[].class),
        paramsCaptor.capture());

    Map<String, Object> params = paramsCaptor.getValue();
    assertEquals(stockEventId, params.get("stockEventId"));
    assertEquals("en", params.get("lang"));
    assertEquals(TIME_ZONE_ID, params.get("timeZoneId"));
    assertEquals(DATE_FORMAT, params.get("dateFormat"));
    assertEquals(DATE_TIME_FORMAT, params.get("dateTimeFormat"));
    assertEquals(createDecimalFormat(), params.get("decimalFormat"));
    assertEquals(true, params.get(SHOW_IN_DOSES));
  }

  @Test
  public void shouldPassShowInPacksFlagToStockEventReport() {
    UUID stockEventId = UUID.randomUUID();

    when(reportService.fillAndExportReport(any(String.class), any(byte[].class), anyMap()))
        .thenReturn(testReportData);

    jasperReportService.generateStockEventReport(stockEventId, "en", false);

    verify(reportService).fillAndExportReport(eq("stockEvent"), any(byte[].class),
        paramsCaptor.capture());
    assertEquals(false, paramsCaptor.getValue().get(SHOW_IN_DOSES));
  }

  @Test
  public void shouldGenerateReportWithProperParamsForPrintPhysicalInventory() throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("dateTimeFormat", DATE_TIME_FORMAT);
    params.put("dateFormat", DATE_FORMAT);
    params.put("format", "pdf");
    params.put("decimalFormat", createDecimalFormat());
    params.put(SHOW_IN_DOSES, false);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(JasperCompileManager.compileReport(getClass().getResourceAsStream(
        PI_LINES_REPORT_URL)));
    JasperTemplate jasperTemplate = new JasperTemplate("test template", bos.toByteArray(), "type",
        "description");
    when(dataSource.getConnection()).thenReturn(mock(Connection.class));
    JasperPrint jasperPrint = mock(JasperPrint.class);
    PowerMockito.when(JasperFillManager.fillReport(any(JasperReport.class), anyMap(),
        any(Connection.class)))
        .thenReturn(jasperPrint);
    PowerMockito.when(JasperExportManager.exportReportToPdf(jasperPrint))
        .thenReturn(testReportData);

    byte[] reportData = jasperReportService.generateReport(jasperTemplate, params);

    assertEquals(testReportData, reportData);
  }

  @Test
  public void shouldPrintOneRowPerAdjustmentForPhysicalInventoryWithAdjustments() {
    StockCardLineItem lineItem = physicalInventoryLine(0, 100,
        StockCardLineItemReason.physicalDebit(),
        adjustment(ReasonType.DEBIT, "Ajustement Inventaire (-)", 100));

    List<StockCardLineItemDto> printed = printStockCard("en", lineItem);

    assertEquals(1, printed.size());
    assertRow(printed.get(0), "Ajustement Inventaire (-)", 100, 0);
  }

  @Test
  public void shouldChainStockOnHandAcrossAdjustmentRows() {
    StockCardLineItem lineItem = physicalInventoryLine(120, 20,
        StockCardLineItemReason.physicalCredit(),
        adjustment(ReasonType.CREDIT, TRANSFER_IN, 30),
        adjustment(ReasonType.DEBIT, DAMAGE, 10));

    List<StockCardLineItemDto> printed = printStockCard("en", lineItem);

    assertEquals(2, printed.size());
    assertRow(printed.get(0), DAMAGE, 10, 120);
    assertRow(printed.get(1), TRANSFER_IN, 30, 130);
  }

  @Test
  public void shouldPrintPhysicalInventoryReasonForPhysicalInventoryWithoutAdjustments() {
    Message physicalInventory = new Message(STOCK_CARD_PHYSICAL_INVENTORY);
    when(messageService.localize(physicalInventory, Locale.ENGLISH))
        .thenReturn(physicalInventory.new LocalizedMessage(PHYSICAL_INVENTORY));
    StockCardLineItem lineItem = physicalInventoryLine(0, 0,
        StockCardLineItemReason.physicalBalance());

    List<StockCardLineItemDto> printed = printStockCard("en", lineItem);

    assertEquals(1, printed.size());
    assertRow(printed.get(0), PHYSICAL_INVENTORY, 0, 0);
  }

  @Test
  public void shouldLocalizePhysicalInventoryReasonInReportLanguage() {
    Message physicalInventory = new Message(STOCK_CARD_PHYSICAL_INVENTORY);
    when(messageService.localize(physicalInventory, Locale.FRENCH))
        .thenReturn(physicalInventory.new LocalizedMessage("Inventaire physique"));
    StockCardLineItem lineItem = physicalInventoryLine(50, 50,
        StockCardLineItemReason.physicalCredit());

    List<StockCardLineItemDto> printed = printStockCard("fr", lineItem);

    assertEquals("Inventaire physique", printed.get(0).getLineItem().getReason().getName());
  }

  @Test
  public void shouldKeepReasonFreeTextOnPhysicalInventoryRows() {
    StockCardLineItem lineItem = physicalInventoryLine(120, 20,
        StockCardLineItemReason.physicalCredit(),
        adjustment(ReasonType.CREDIT, TRANSFER_IN, 30),
        adjustment(ReasonType.DEBIT, DAMAGE, 10));
    lineItem.setReasonFreeText("note");

    List<StockCardLineItemDto> printed = printStockCard("en", lineItem);

    assertEquals("note", printed.get(0).getLineItem().getReasonFreeText());
    assertEquals("note", printed.get(1).getLineItem().getReasonFreeText());
  }

  @Test
  public void shouldPrintNonPhysicalInventoryLineItemsUnchanged() {
    StockCardLineItem lineItem = StockCardLineItem.builder()
        .occurredDate(LocalDate.of(2017, 2, 13))
        .reason(reason(ReasonType.DEBIT, ReasonCategory.ADJUSTMENT, DAMAGE))
        .quantity(5)
        .stockOnHand(95)
        .build();

    List<StockCardLineItemDto> printed = printStockCard("en", lineItem);

    assertEquals(1, printed.size());
    assertRow(printed.get(0), DAMAGE, 5, 95);
  }

  private List<StockCardLineItemDto> printStockCard(String lang,
      StockCardLineItem... lineItems) {
    StockCardDto stockCard = StockCardDtoDataBuilder.createStockCardDto();
    List<StockCardLineItemDto> lineItemDtos = new ArrayList<>();
    for (StockCardLineItem lineItem : lineItems) {
      lineItemDtos.add(StockCardLineItemDto.builder().lineItem(lineItem).build());
    }
    stockCard.setLineItems(lineItemDtos);
    when(stockCardService.findStockCardById(stockCard.getId())).thenReturn(stockCard);
    when(reportService.fillAndExportReport(any(String.class), any(byte[].class), anyMap()))
        .thenReturn(testReportData);

    jasperReportService.generateStockCardReport(stockCard.getId(), lang, true);

    verify(reportService).fillAndExportReport(eq("stockCard"), any(byte[].class),
        paramsCaptor.capture());
    @SuppressWarnings("unchecked")
    List<StockCardDto> datasource = (List<StockCardDto>) paramsCaptor.getValue()
        .get("datasource");
    return datasource.get(0).getLineItems();
  }

  private void assertRow(StockCardLineItemDto row, String reasonName, int quantity,
      int stockOnHand) {
    assertEquals(reasonName, row.getLineItem().getReason().getName());
    assertEquals(Integer.valueOf(quantity), row.getLineItem().getQuantity());
    assertEquals(Integer.valueOf(stockOnHand), row.getLineItem().getStockOnHand());
  }

  private StockCardLineItem physicalInventoryLine(int stockOnHand, int quantity,
      StockCardLineItemReason generatedReason,
      PhysicalInventoryLineItemAdjustment... adjustments) {
    return StockCardLineItem.builder()
        .occurredDate(LocalDate.of(2026, 9, 10))
        .reason(generatedReason)
        .quantity(quantity)
        .stockOnHand(stockOnHand)
        .stockAdjustments(new ArrayList<>(Arrays.asList(adjustments)))
        .build();
  }

  private PhysicalInventoryLineItemAdjustment adjustment(ReasonType type, String name,
      int quantity) {
    return PhysicalInventoryLineItemAdjustment.builder()
        .reason(reason(type, ReasonCategory.ADJUSTMENT, name))
        .quantity(quantity)
        .build();
  }

  private StockCardLineItemReason reason(ReasonType type, ReasonCategory category,
      String name) {
    return StockCardLineItemReason.builder()
        .reasonType(type)
        .reasonCategory(category)
        .name(name)
        .build();
  }

  private DecimalFormat createDecimalFormat() {
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(GROUPING_SEPARATOR.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.parseInt(GROUPING_SIZE));
    return decimalFormat;
  }
}