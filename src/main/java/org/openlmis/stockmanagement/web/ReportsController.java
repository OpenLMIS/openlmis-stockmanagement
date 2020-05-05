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

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.UUID;
import org.openlmis.stockmanagement.service.JasperReportService;
import org.openlmis.stockmanagement.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class ReportsController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReportsController.class);

  @Autowired
  private JasperReportService reportService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Get stock card report in PDF format.
   *
   * @param stockCardId stock card id.
   * @return generated PDF report
   */
  @RequestMapping(value = "/stockCards/{id}/print", method = GET)
  @ResponseBody
  public ResponseEntity<byte[]> getStockCard(@PathVariable("id") UUID stockCardId) {
    LOGGER.info("Try to generate stock card report with id: {}", stockCardId);

    byte[] report = reportService.generateStockCardReport(stockCardId);

    return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition", "inline; filename=stock_card_" + stockCardId + ".pdf")
        .body(report);
  }


  /**
   * Get stock card summaries report by program and facility.
   *
   * @return generated PDF report
   */
  @RequestMapping(value = "/stockCardSummaries/print", method = GET)
  @ResponseBody
  public ResponseEntity<byte[]> getStockCardSummaries(
      @RequestParam("program") UUID program,
      @RequestParam("facility") UUID facility) {
    LOGGER.info("Try to generate stock card summaries report by program %s and facility %s.",
        program.toString(), facility.toString());
    permissionService.canViewStockCard(program, facility);
    byte[] report = reportService.generateStockCardSummariesReport(program, facility);

    return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header("Content-Disposition",
            "inline; filename=stock_card_summaries" + program + "_" + facility + ".pdf")
        .body(report);
  }
}
