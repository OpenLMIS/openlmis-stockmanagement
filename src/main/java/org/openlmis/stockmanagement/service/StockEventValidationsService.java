package org.openlmis.stockmanagement.service;


import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.validators.StockEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockEventValidationsService {

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private List<StockEventValidator> stockEventValidators;

  public void validate(StockEventDto stockEventDto) {
    permissionService
        .canCreateStockEvent(stockEventDto.getProgramId(), stockEventDto.getFacilityId());

    stockEventValidators.forEach(validator -> validator.validate(stockEventDto));
  }
}
