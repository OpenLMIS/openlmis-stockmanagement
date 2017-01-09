package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.repository.StockCardTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockCardTemplateService {
  @Autowired
  private StockCardTemplateRepository stockCardTemplateRepository;

  public StockCardTemplate saveOrUpdate(StockCardTemplate template) {
    StockCardTemplate found = stockCardTemplateRepository.findByProgramIdAndFacilityTypeId(
            template.getProgramId(), template.getFacilityTypeId());

    if (found != null) {
      template.setId(found.getId());
    }

    return stockCardTemplateRepository.save(template);
  }
}
