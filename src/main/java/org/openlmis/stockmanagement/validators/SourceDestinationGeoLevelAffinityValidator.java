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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.ValidDestinationService;
import org.openlmis.stockmanagement.service.ValidSourceService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This validator check geo level affinity to make sure that chosen destination/source is correct.
 * Meaning that chosen source or destination must be from/to valid node which match the
 * the geography zone (of the affinity level) with provided facility.
 */
@Component(value = "SourceDestinationGeoLevelAffinityValidator")
public class SourceDestinationGeoLevelAffinityValidator implements StockEventValidator {

  @Autowired
  private ValidDestinationService validDestinationService;

  @Autowired
  private ValidSourceService validSourceService;

  @Override
  public void validate(StockEventDto stockEventDto) {
    LOGGER.debug("Validate geo level affinity");
    
    if (!stockEventDto.hasLineItems()) {
      return;
    }

    List<StockEventLineItemDto> stockEventLineItems = stockEventDto.getLineItems();
    
    if (stockEventLineItems.get(0).getSourceId() != null) {
      validateSources(stockEventDto);
    }
    if (stockEventLineItems.get(0).getDestinationId() != null) {
      validateDestinations(stockEventDto);
    }

  }

  private void  validateDestinations(StockEventDto stockEventDto) {
    List<ValidSourceDestinationDto> validDestinationDtos = validDestinationService
        .findDestinations(stockEventDto.getProgramId(), stockEventDto.getFacilityId());

    List<UUID> validDestinationDtoIds = getValidNodeIds(validDestinationDtos);

    List<StockEventLineItemDto> stockEventLineItemsNotMatch = 
        findAllStockEventLineItemsWithNoAffinityMatch(stockEventDto.getLineItems(),
          lineItem -> isNotGeoLevelAffinity(lineItem.getDestinationId(), validDestinationDtoIds));
    
    if (stockEventLineItemsNotMatch.isEmpty()) {
      return;
    }
    
    UUID notValidNodeAssignmentId = stockEventLineItemsNotMatch.get(0).getDestinationId();
    FacilityDto facilityDto = stockEventDto.getContext().getFacility();
    throwError(ERROR_DESTINATION_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY, 
        facilityDto.getName(), notValidNodeAssignmentId);

  }

  private void  validateSources(StockEventDto stockEventDto) {
    List<ValidSourceDestinationDto> validSourceDtos =
        validSourceService.findSources(stockEventDto.getProgramId(), stockEventDto.getFacilityId());

    List<UUID> validSourceDtoIds = getValidNodeIds(validSourceDtos);

    List<StockEventLineItemDto> stockEventLineItemsNotMatch = 
        findAllStockEventLineItemsWithNoAffinityMatch(stockEventDto.getLineItems(),
          lineItem -> isNotGeoLevelAffinity(lineItem.getSourceId(), validSourceDtoIds));
     
    if (stockEventLineItemsNotMatch.isEmpty()) {
      return;
    }

    UUID notValidNodeAssignmentId = stockEventLineItemsNotMatch.get(0).getSourceId();
    FacilityDto facilityDto = stockEventDto.getContext().getFacility();
    throwError(ERROR_SOURCE_ASSIGNMENT_NO_MATCH_GEO_LEVEL_AFFINITY, 
        facilityDto.getName(), notValidNodeAssignmentId);
    
  }

  private List<StockEventLineItemDto> findAllStockEventLineItemsWithNoAffinityMatch(
      List<StockEventLineItemDto> stockEventLineItems,
      Predicate<StockEventLineItemDto> checkGeoLevelAffinity) {
    return stockEventLineItems.stream()
        .filter(checkGeoLevelAffinity)
        .collect(Collectors.toList());
  }

  private List<UUID> getValidNodeIds(List<ValidSourceDestinationDto> validSourceDtos) {
    return validSourceDtos.stream()
        .map(validSourceDestinationDto -> validSourceDestinationDto.getNode().getId())
        .collect(Collectors.toList());
  }

  private boolean isNotGeoLevelAffinity(UUID nodeId,
      List<UUID> sourceDestinationAssignments) {
    return !sourceDestinationAssignments.contains(nodeId);
  }

  private void throwError(String messageKey, Object... params) {
    throw new ValidationMessageException(new Message(messageKey, params));
  }
}

