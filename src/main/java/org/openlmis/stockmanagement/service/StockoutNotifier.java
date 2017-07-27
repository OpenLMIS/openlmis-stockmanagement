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

import static org.openlmis.stockmanagement.i18n.MessageKeys.EMAIL_ACTION_REQUIRED_CONTENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.EMAIL_ACTION_REQUIRED_SUBJECT;
import static org.openlmis.stockmanagement.service.PermissionService.STOCK_INVENTORIES_EDIT;

import org.apache.commons.lang.text.StrSubstitutor;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.referencedata.RightDto;
import org.openlmis.stockmanagement.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.service.referencedata.RightReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisingUsersReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.SupervisoryNodeReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

  /**
   * Notify user with "Edit stock inventories" right for the facility/program that
   * facility has stocked out of a product.
   *
   * @param stockCard stockCard for a product
   */
  public void notifyStockEditors(StockCard stockCard) {
    Collection<UserDto> recipents = getEditors(stockCard);

    String subject = getMessage(EMAIL_ACTION_REQUIRED_SUBJECT);
    String content = getMessage(EMAIL_ACTION_REQUIRED_CONTENT);

    Map<String, String> valuesMap = getValuesMap();
    StrSubstitutor sub = new StrSubstitutor(valuesMap);
    for (UserDto recipent : recipents) {
      if (recipent.getHomeFacility().getId() == stockCard.getFacilityId()
          && canBeNotified(recipent)) {
        valuesMap.put("recipent", recipent.getUsername());
        notificationService.notify(recipent, subject, sub.replace(content));
      }
    }
  }

  private Collection<UserDto> getEditors(StockCard stockCard) {
    RightDto right = rightReferenceDataService.findRight(STOCK_INVENTORIES_EDIT);
    SupervisoryNodeDto supervisoryNode = supervisoryNodeReferenceDataService
        .findSupervisoryNode(stockCard.getProgramId(), stockCard.getFacilityId());

    return supervisingUsersReferenceDataService
        .findAll(supervisoryNode.getId(), right.getId(), stockCard.getProgramId());
  }

  private Map<String, String> getValuesMap() {
    Map<String, String> valuesMap = new HashMap<>();
    return valuesMap;
  }

}
