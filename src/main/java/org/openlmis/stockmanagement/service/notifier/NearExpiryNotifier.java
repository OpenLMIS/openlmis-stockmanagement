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

import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_NEAR_EXPIRY_CONTENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.NOTIFICATION_NEAR_EXPIRY_SUBJECT;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NearExpiryNotifier {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(NearExpiryNotifier.class);

  @Autowired
  LotReferenceDataService lotReferenceDataService;

  @Autowired
  RightReferenceDataService rightReferenceDataService;

  @Autowired
  StockCardRepository stockCardRepository;
  
  @Autowired
  StockCardNotifier stockCardNotifier;

  @Value("${time.zoneId}")
  private String timeZoneId;

  private Map<UUID, LotDto> expiringLotMap;
  
  private LocalDate expirationDate;

  /**
   * Check stock cards with lots that have a certain expiration date. If any are found, notify stock
   * card owners.
   */
  @Scheduled(cron = "${stockmanagement.nearExpiry.cron}", zone = "${time.zoneId}")
  public void checkNearExpiryAndNotify() {
    // Expiration of six months from today, OLMIS-3186
    expirationDate = LocalDate.now(ZoneId.of(timeZoneId)).plusMonths(6);
    XLOGGER.debug("Expiration date = {}", expirationDate);
    expiringLotMap = lotReferenceDataService.getAllLotsExpiringOn(expirationDate)
        .stream()
        .collect(Collectors.toMap(LotDto::getId, Function.identity()));
    Collection<UUID> expiringLotIds = expiringLotMap.keySet();
    XLOGGER.debug("Expiring Lot IDs = {}", expiringLotIds);
    
    List<StockCard> expiringStockCards = stockCardRepository.findByLotIdIn(expiringLotIds);
    XLOGGER.debug("Expiring Stock Card IDs = {}", expiringStockCards.stream()
        .map(StockCard::getId)
        .collect(Collectors.toList()));

    RightDto right = rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT);
    UUID rightId = right.getId();
    expiringStockCards.forEach(card -> {
      NotificationMessageParams params = new NotificationMessageParams(
          NOTIFICATION_NEAR_EXPIRY_SUBJECT, NOTIFICATION_NEAR_EXPIRY_CONTENT,
          constructSubstitutionMap(card));
      stockCardNotifier.notifyStockEditors(card, rightId, params);
    });
  }

  Map<String, String> constructSubstitutionMap(StockCard stockCard) {
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("facilityName", stockCardNotifier.getFacilityName(stockCard.getFacilityId()));
    valuesMap.put("programName", stockCardNotifier.getProgramName(stockCard.getProgramId()));
    valuesMap.put("orderableName", stockCardNotifier.getOrderableName(stockCard.getOrderableId()));
    LotDto lot = expiringLotMap.get(stockCard.getLotId());
    valuesMap.put("lotCode", null != lot ? lot.getLotCode() : "");
    valuesMap.put("expirationDate", stockCardNotifier.getDateFormatter().format(expirationDate));
    valuesMap.put("urlToViewBinCard", stockCardNotifier.getUrlToViewBinCard(stockCard.getId()));
    return valuesMap;
  }
}
