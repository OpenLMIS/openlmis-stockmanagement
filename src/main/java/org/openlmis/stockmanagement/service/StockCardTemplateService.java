package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.repository.StockCardTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * A service that wraps around repository to provide ability
 * to save or update stock card template.
 */
@Service
public class StockCardTemplateService {
  @Autowired
  private StockCardTemplateRepository stockCardTemplateRepository;

  /**
   * Save or update stock card template by facility type id and program id.
   *
   * @param template object to save or update.
   * @return the saved or updated object.
   */
  public StockCardTemplate saveOrUpdate(StockCardTemplate template) {
    StockCardTemplate found = stockCardTemplateRepository.findByProgramIdAndFacilityTypeId(
            template.getProgramId(), template.getFacilityTypeId());

    if (found != null) {
      template.setId(found.getId());
    }

    return stockCardTemplateRepository.save(template);
  }

  /**
   * Find stock card template by facility type id and program id.
   *
   * @param programId      program id.
   * @param facilityTypeId facility type id.
   * @return the found template or null if not found.
   */
  public StockCardTemplate findByProgramIdAndFacilityTypeId(UUID programId, UUID facilityTypeId) {
    return stockCardTemplateRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
  }
}
