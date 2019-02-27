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

package org.openlmis.stockmanagement.service.notifier;

import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_STOCKOUT_CONTENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_STOCKOUT_SUBJECT;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StockoutNotifier extends StockCardNotifier {

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  @Value("${email.urlToInitiateRequisition}")
  private String urlToInitiateRequisition;

  @Override
  String getMessageSubject() {
    return NOTIFICATION_STOCKOUT_SUBJECT;
  }

  @Override
  String getMessageContent() {
    return NOTIFICATION_STOCKOUT_CONTENT;
  }

  @Override
  Map<String, String> getValuesMap(StockCard stockCard) {
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("facilityName", getFacilityName(stockCard.getFacilityId()));
    valuesMap.put("orderableName", getOrderableName(stockCard.getOrderableId()));
    valuesMap.put("orderableNameLotInformation",
        getOrderableNameLotInformation(valuesMap.get("orderableName"), stockCard.getLotId()));
    valuesMap.put("programName", getProgramName(stockCard.getProgramId()));

    List<StockCardLineItem> lineItems = stockCard.getLineItems();
    LocalDate stockoutDate = lineItems.get(lineItems.size() - 1).getOccurredDate();
    valuesMap.put("stockoutDate", getDateFormatter().format(stockoutDate));
    long numberOfDaysOfStockout = getNumberOfDaysOfStockout(stockoutDate);
    valuesMap.put("numberOfDaysOfStockout", numberOfDaysOfStockout
        + (numberOfDaysOfStockout == 1 ? " day" : " days"));

    valuesMap.put("urlToViewBinCard", getUrlToViewBinCard(stockCard));
    valuesMap.put("urlToInitiateRequisition", getUrlToInitiateRequisition(stockCard));
    return valuesMap;
  }

  private String getOrderableNameLotInformation(String orderableName, UUID lotId) {
    if (lotId != null) {
      LotDto lot = lotReferenceDataService.findOne(lotId);
      return orderableName + " " + lot.getLotCode();
    }
    return orderableName;
  }

  private long getNumberOfDaysOfStockout(LocalDate stockoutDate) {
    return ChronoUnit.DAYS.between(stockoutDate, LocalDate.now());
  }

  private String getUrlToInitiateRequisition(StockCard stockCard) {
    return MessageFormat.format(urlToInitiateRequisition,
        stockCard.getFacilityId(), stockCard.getProgramId(), "true", "false");
  }
}
