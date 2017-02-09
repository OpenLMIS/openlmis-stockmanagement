package org.openlmis.stockmanagement.service;


import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.validators.StockEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StockEventValidationsService {

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private List<StockEventValidator> stockEventValidators;

  /**
   * Validate stock event with permission service and all validators.
   *
   * @param stockEventDto the event to be validated.
   */
  public void validate(StockEventDto stockEventDto) {
    stockEventValidators.forEach(validator -> validator.validate(stockEventDto));
    checkPermission(stockEventDto);
  }

  private void checkPermission(StockEventDto stockEventDto) {
    UUID programId = stockEventDto.getProgramId();
    UUID facilityId = stockEventDto.getFacilityId();
    if (!stockEventDto.hasAlternativeStockCardIdentifier()) {
      StockCard stockCard = stockCardRepository.findOne(stockEventDto.getStockCardId());
      programId = stockCard.getProgramId();
      facilityId = stockCard.getFacilityId();
    }
    permissionService.canCreateStockEvent(programId, facilityId);
  }
}
