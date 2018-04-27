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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_NOT_FOUND;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockCardLineItemReasonDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardLineItemReasonService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/api")
public class StockCardLineItemReasonController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(StockCardLineItemReasonController.class);

  @Autowired
  private StockCardLineItemReasonService reasonService;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Create a new stock card line item reason. If the ID is specified, ID will be ignored.
   *
   * @param reason a stock card line item reason bound to request body
   * @return created stock card line item reason
   */
  @RequestMapping(value = "stockCardLineItemReasons", method = POST)
  @ResponseBody
  @ResponseStatus(CREATED)
  public StockCardLineItemReasonDto createReason(@RequestBody StockCardLineItemReasonDto reason) {
    LOGGER.debug("Try to create a new stock card line item reason");
    permissionService.canManageReasons();
    reason.setId(null);
    return StockCardLineItemReasonDto.newInstance(
        reasonService.saveOrUpdate(StockCardLineItemReason.newInstance(reason))
    );
  }

  /**
   * Retrieve all stock card line item reasons.
   *
   * @return list of all reasons.
   */
  @RequestMapping(value = "stockCardLineItemReasons", method = GET)
  @ResponseBody
  public List<StockCardLineItemReasonDto> getAllReasons() {
    return reasonRepository
        .findAll()
        .stream()
        .map(StockCardLineItemReasonDto::newInstance)
        .collect(Collectors.toList());
  }

  /**
   * Retrieve a stock card line item reason based on id.
   */
  @RequestMapping(value = "stockCardLineItemReasons/{id}", method = GET)
  @ResponseBody
  public StockCardLineItemReasonDto getReason(@PathVariable("id") UUID reasonId) {
    StockCardLineItemReason reason = reasonRepository.findOne(reasonId);

    if (null == reason) {
      throw new ValidationMessageException(new Message(ERROR_REASON_NOT_FOUND));
    }

    return StockCardLineItemReasonDto.newInstance(reason);
  }

  /**
   * Update a stock card line item reason.
   *
   * @param reasonId ID of the reason would be updated
   * @param reason   a stock card line item reason bound to request body
   * @return updated stock card line item reason
   */
  @RequestMapping(value = "stockCardLineItemReasons/{id}", method = PUT)
  @ResponseBody
  public StockCardLineItemReasonDto updateReason(@PathVariable("id") UUID reasonId,
      @RequestBody StockCardLineItemReasonDto reason) {
    permissionService.canManageReasons();
    LOGGER.debug("Try to update stock card line item reason with id: ", reasonId.toString());
    reason.setId(reasonId);
    reasonService.checkUpdateReasonIdExists(reasonId);
    return StockCardLineItemReasonDto.newInstance(
        reasonService.saveOrUpdate(StockCardLineItemReason.newInstance(reason))
    );
  }

}
