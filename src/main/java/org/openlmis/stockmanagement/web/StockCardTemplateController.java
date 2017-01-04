package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.repository.StockCardTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Controller used for creating/getting stock card templates.
 */
@Controller
@RequestMapping("/api")
public class StockCardTemplateController {

  @Autowired
  private StockCardTemplateRepository repository;

  /**
   * Search for stock card template by program id and facility type id.
   *
   * @param program      Program id
   * @param facilityType Facility type id
   * @return The found stock card template, or 404 when not found.
   */
  @RequestMapping(value = "/stockCardTemplate", method = GET)
  public ResponseEntity<StockCardTemplate> searchStockCardTemplate(
          @RequestParam UUID program,
          @RequestParam UUID facilityType
  ) {

    StockCardTemplate foundTemplate = repository
            .findByProgramIdAndFacilityTypeId(program, facilityType);

    if (foundTemplate == null) {
      return new ResponseEntity<>(NOT_FOUND);
    }
    return new ResponseEntity<>(foundTemplate, OK);
  }

}
