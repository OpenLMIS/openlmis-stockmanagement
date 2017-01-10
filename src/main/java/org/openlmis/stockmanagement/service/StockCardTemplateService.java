package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.dto.StockCardFieldDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemFieldDto;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.openlmis.stockmanagement.repository.AvailableStockCardFieldsRepository;
import org.openlmis.stockmanagement.repository.AvailableStockCardLineItemFieldsRepository;
import org.openlmis.stockmanagement.repository.StockCardTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

/**
 * A service that wraps around repository to provide ability
 * to save or update stock card template.
 */
@Service
public class StockCardTemplateService {

  @Autowired
  private StockCardTemplateRepository stockCardTemplateRepository;

  @Autowired
  private AvailableStockCardFieldsRepository cardFieldsRepository;

  @Autowired
  private AvailableStockCardLineItemFieldsRepository lineItemFieldsRepository;

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
      stockCardTemplateRepository.delete(found);
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

  /**
   * Get default stock card template, with all fields set to false.
   *
   * @return default stock card template.
   */
  public StockCardTemplateDto getDefaultStockCardTemplate() {
    List<StockCardFieldDto> cardFieldDtos =
            findAllFields(cardFieldsRepository, this::convertModelToDto);

    List<StockCardLineItemFieldDto> lineItemFieldDtos =
            findAllFields(lineItemFieldsRepository, this::convertModelToDto);

    StockCardTemplateDto dto = new StockCardTemplateDto();
    dto.setStockCardFields(cardFieldDtos);
    dto.setStockCardLineItemFields(lineItemFieldDtos);

    return dto;
  }

  private <F, T> List<T> findAllFields(PagingAndSortingRepository<F, UUID> repo,
                                       Function<F, T> converter) {
    return stream(repo.findAll().spliterator(), false)
            .map(converter)
            .collect(Collectors.toList());
  }

  private StockCardFieldDto convertModelToDto(AvailableStockCardFields model) {
    return new StockCardFieldDto(model.getName(), false, 0);
  }

  private StockCardLineItemFieldDto convertModelToDto(AvailableStockCardLineItemFields model) {
    return new StockCardLineItemFieldDto(model.getName(), false, 0);
  }
}
