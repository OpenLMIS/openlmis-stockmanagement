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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.text.StrSubstitutor;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.service.notification.NotificationService;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisingUsersReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisoryNodeReferenceDataService;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public abstract class StockCardNotifier extends BaseNotifier {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(StockCardNotifier.class);

  @Autowired
  private SupervisingUsersReferenceDataService supervisingUsersReferenceDataService;

  @Autowired
  private SupervisoryNodeReferenceDataService supervisoryNodeReferenceDataService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Value("${email.urlToViewBinCard}")
  private String urlToViewBinCard;

  /**
   * Notify user with "Edit stock inventories" right for the facility/program that facility has
   * stocked out of a product.
   *
   * @param stockCard StockCard for a product
   * @param rightId right UUID
   */
  @Async
  public void notifyStockEditors(StockCard stockCard, UUID rightId) {
    Profiler profiler = new Profiler("NOTIFY_STOCK_EDITORS");
    profiler.setLogger(XLOGGER);

    profiler.start("GET_EDITORS");
    Collection<UserDto> recipients = getEditors(stockCard, rightId);

    String subject = getMessage(getMessageSubject());
    String content = getMessage(getMessageContent());

    Map<String, String> valuesMap = getValuesMap(stockCard);
    StrSubstitutor sub = new StrSubstitutor(valuesMap);

    profiler.start("NOTIFY_RECIPIENTS");
    for (UserDto recipient : recipients) {
      if (recipient.getHomeFacilityId().equals(stockCard.getFacilityId())) {
        valuesMap.put("username", recipient.getUsername());
        XLOGGER.debug("Recipient username = {}", recipient.getUsername());
        notificationService.notify(recipient, sub.replace(subject), sub.replace(content));
      }
    }

    profiler.stop().log();
  }

  private Collection<UserDto> getEditors(StockCard stockCard, UUID rightId) {
    SupervisoryNodeDto supervisoryNode = supervisoryNodeReferenceDataService
        .findSupervisoryNode(stockCard.getProgramId(), stockCard.getFacilityId());

    if (supervisoryNode == null) {
      throw new IllegalArgumentException(
          String.format("There is no supervisory node for program %s and facility %s",
              stockCard.getProgramId(), stockCard.getFacilityId()));
    }
    
    XLOGGER.debug("Supervisory node ID = {}", supervisoryNode.getId());

    return supervisingUsersReferenceDataService
        .findAll(supervisoryNode.getId(), rightId, stockCard.getProgramId());
  }

  abstract String getMessageSubject();
  
  abstract String getMessageContent();
  
  abstract Map<String, String> getValuesMap(StockCard stockCard);

  String getFacilityName(UUID facilityId) {
    return facilityReferenceDataService.findOne(facilityId).getName();
  }

  String getProgramName(UUID programId) {
    return programReferenceDataService.findOne(programId).getName();
  }

  String getOrderableName(UUID orderableId) {
    return orderableReferenceDataService.findOne(orderableId).getFullProductName();
  }

  String getUrlToViewBinCard(StockCard stockCard) {
    return MessageFormat.format(urlToViewBinCard, stockCard.getId());
  }
}
