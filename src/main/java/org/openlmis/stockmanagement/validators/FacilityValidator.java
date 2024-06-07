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

package org.openlmis.stockmanagement.validators;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_MUST_BE_WARD_SERVICE_OF_FACILITY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_NODE_NOT_FOUND;

import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.NodeRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "FacilityValidator")
public class FacilityValidator implements StockEventValidator {

  @Autowired
  private FacilityReferenceDataService facilityService;

  @Autowired
  private NodeRepository nodeRepository;

  @Override
  public void validate(StockEventDto stockEventDto) {
    XLOGGER.entry(stockEventDto);

    Profiler profiler = new Profiler("FACILITY_VALIDATOR");
    profiler.setLogger(XLOGGER);

    if (stockEventDto.isPhysicalInventory() || stockEventDto.getLineItems().isEmpty()) {
      return;
    }

    FacilityDto facility = facilityService.findOne(stockEventDto.getFacilityId());
    for (StockEventLineItemDto lineItem : stockEventDto.getLineItems()) {
      if (lineItem.getDestinationId() != null) {
        Node node = nodeRepository.findById(lineItem.getDestinationId())
            .orElseThrow(() -> new ResourceNotFoundException(
                new Message(ERROR_NODE_NOT_FOUND, lineItem.getDestinationId())));
        if (node.isRefDataFacility()) {
          FacilityDto destinationFacility = facilityService.findOne(node.getReferenceId());
          if (destinationFacility.getType().getCode().equals("WS")
              && !destinationFacility.getGeographicZone().getId()
              .equals(facility.getGeographicZone().getId())) {
            throw new ValidationMessageException(
                new Message(ERROR_DESTINATION_MUST_BE_WARD_SERVICE_OF_FACILITY));
          }
        }
      }
    }

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
  }

}
