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

import static org.openlmis.stockmanagement.i18n.MessageKeys.EMAIL_ACTION_REQUIRED_CONTENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.EMAIL_ACTION_REQUIRED_SUBJECT;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;

import org.apache.commons.lang.text.StrSubstitutor;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.service.NotificationService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisingUsersReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisoryNodeReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class StockoutNotifier extends BaseNotifier {

  @Autowired
  private RightReferenceDataService rightReferenceDataService;

  @Autowired
  private SupervisingUsersReferenceDataService supervisingUsersReferenceDataService;

  @Autowired
  private SupervisoryNodeReferenceDataService supervisoryNodeReferenceDataService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private LotReferenceDataService lotReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Value("${email.urlToViewBinCard}")
  private String urlToViewBinCard;

  @Value("${email.urlToInitiateRequisition}")
  private String urlToInitiateRequisition;

  /**
   * Notify user with "Edit stock inventories" right for the facility/program that
   * facility has stocked out of a product.
   *
   * @param stockCard StockCard for a product
   */
  public void notifyStockEditors(StockCard stockCard) {
    Collection<UserDto> recipients = getEditors(stockCard);

    String subject = getMessage(EMAIL_ACTION_REQUIRED_SUBJECT);
    String content = getMessage(EMAIL_ACTION_REQUIRED_CONTENT);

    Map<String, String> valuesMap = getValuesMap(stockCard);
    StrSubstitutor sub = new StrSubstitutor(valuesMap);
    for (UserDto recipient : recipients) {
      if (recipient.getHomeFacility().getId().equals(stockCard.getFacilityId())
          && canBeNotified(recipient)) {
        valuesMap.put("username", recipient.getUsername());
        notificationService.notify(recipient, sub.replace(subject), sub.replace(content));
      }
    }
  }

  private Collection<UserDto> getEditors(StockCard stockCard) {
    RightDto right = rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT);
    SupervisoryNodeDto supervisoryNode = supervisoryNodeReferenceDataService
        .findSupervisoryNode(stockCard.getProgramId(), stockCard.getFacilityId());

    if (supervisoryNode == null) {
      throw new IllegalArgumentException(
              String.format("There is no supervisory node for program %s and facility %s",
              stockCard.getProgramId(), stockCard.getFacilityId()));
    }

    return supervisingUsersReferenceDataService
        .findAll(supervisoryNode.getId(), right.getId(), stockCard.getProgramId());
  }

  private Map<String, String> getValuesMap(StockCard stockCard) {
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("facilityName", getFacilityName(stockCard.getFacilityId()));
    valuesMap.put("orderableName", getOrderableName(stockCard.getOrderableId()));
    valuesMap.put("orderableNameLotInformation",
        getOrderableNameLotInformation(valuesMap.get("orderableName"), stockCard.getLotId()));
    valuesMap.put("programName", getProgramName(stockCard.getProgramId()));

    List<StockCardLineItem> lineItems = stockCard.getLineItems();
    ZonedDateTime stockoutDate = lineItems.get(lineItems.size() - 1).getOccurredDate();
    valuesMap.put("stockoutDate", getDateTimeFormatter().format(stockoutDate));
    long numberOfDaysOfStockout = getNumberOfDaysOfStockout(stockoutDate);
    valuesMap.put("numberOfDaysOfStockout", numberOfDaysOfStockout
        + (numberOfDaysOfStockout == 1 ? " day" : " days"));

    valuesMap.put("urlToViewBinCard", getUrlToViewBinCard(stockCard));
    valuesMap.put("urlToInitiateRequisition", getUrlToInitiateRequisition(stockCard));
    return valuesMap;
  }

  private String getFacilityName(UUID facilityId) {
    return facilityReferenceDataService.findOne(facilityId).getName();
  }

  private String getOrderableName(UUID orderableId) {
    return orderableReferenceDataService.findOne(orderableId).getFullProductName();
  }

  private String getOrderableNameLotInformation(String orderableName, UUID lotId) {
    if (lotId != null) {
      LotDto lot = lotReferenceDataService.findOne(lotId);
      return orderableName + " " + lot.getLotCode();
    }
    return orderableName;
  }

  private String getProgramName(UUID programId) {
    return programReferenceDataService.findOne(programId).getName();
  }

  private long getNumberOfDaysOfStockout(ZonedDateTime stockoutDate) {
    return ChronoUnit.DAYS.between(stockoutDate, ZonedDateTime.now());
  }

  private String getUrlToViewBinCard(StockCard stockCard) {
    return MessageFormat.format(urlToViewBinCard, stockCard.getId(), stockCard.getFacilityId(),
        stockCard.getProgramId(), "false");
  }

  private String getUrlToInitiateRequisition(StockCard stockCard) {
    return MessageFormat.format(urlToInitiateRequisition,
        stockCard.getFacilityId(), stockCard.getProgramId(), "true", "false");
  }
}
