package org.openlmis.stockmanagement.web;

import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.exception.MissingPermissionException;
import org.openlmis.stockmanagement.repository.StockCardTemplateRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller used for creating/getting stock card templates.
 */
@Controller
@RequestMapping("/api")
public class StockCardTemplateController {

  @Autowired
  private StockCardTemplateRepository repository;

  @Autowired
  private PermissionService permissionService;

  /**
   * Search for stock card template by program id and facility type id.
   *
   * @param program      Program id
   * @param facilityType Facility type id
   * @return The found stock card template, or 404 when not found.
   */
  @RequestMapping(value = "/stockCardTemplates", method = GET)
  public ResponseEntity<StockCardTemplate> searchStockCardTemplate(
      @RequestParam(required = false) UUID program,
      @RequestParam(required = false) UUID facilityType) {

    boolean isRequestingForDefaultTemplate = program == null && facilityType == null;

    if (isRequestingForDefaultTemplate) {
      return new ResponseEntity<>(new StockCardTemplate(), OK);
    } else {
      return searchForExistingTemplate(program, facilityType);
    }
  }

  /**
   * Create stock card template.
   *
   * @param stockCardTemplate a stock card template bound to request body.
   * @return The created stock card template.
   */
  @RequestMapping(value = "/stockCardTemplates", method = POST)
  public ResponseEntity<StockCardTemplate> createStockCardTemplate(
      @RequestBody StockCardTemplate stockCardTemplate) throws MissingPermissionException {

    permissionService.canCreateStockCardTemplate();
    StockCardTemplate newStockCardTemplate = repository.save(stockCardTemplate);
    return new ResponseEntity<>(newStockCardTemplate, CREATED);
  }

  private ResponseEntity<StockCardTemplate> searchForExistingTemplate(
      UUID program, UUID facilityType) {

    StockCardTemplate foundTemplate = repository
        .findByProgramIdAndFacilityTypeId(program, facilityType);

    if (foundTemplate != null) {
      return new ResponseEntity<>(foundTemplate, OK);
    } else {
      return new ResponseEntity<>(NOT_FOUND);
    }
  }
}
