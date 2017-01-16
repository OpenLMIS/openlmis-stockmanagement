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
import org.openlmis.stockmanagement.service.referencedata.FacilityTypeReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * A service that wraps around repository to provide ability
 * to save or update stock card template.
 */
@Service
public class StockCardTemplateService {

  @Autowired
  private StockCardTemplateRepository templateRepository;

  @Autowired
  private AvailableStockCardFieldsRepository cardFieldsRepo;

  @Autowired
  private AvailableStockCardLineItemFieldsRepository lineItemFieldsRepo;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private FacilityTypeReferenceDataService facilityTypeReferenceDataService;

  /**
   * Save or update stock card template by facility type id and program id.
   *
   * @param templateDto object to save or update.
   * @return the saved or updated object.
   */
  @Transactional
  public StockCardTemplateDto saveOrUpdate(StockCardTemplateDto templateDto) {

    StockCardTemplate template = templateDto.toModel(
            findAllFieldsFrom(cardFieldsRepo).collect(toList()),
            findAllFieldsFrom(lineItemFieldsRepo).collect(toList()));

    StockCardTemplate found = templateRepository.findByProgramIdAndFacilityTypeId(
            template.getProgramId(), template.getFacilityTypeId());

    if (found != null) {
      template.setId(found.getId());
      templateRepository.delete(found);
    } else {
      checkProgramAndFacilityTypeExist(template.getProgramId(), template.getFacilityTypeId());
    }

    return StockCardTemplateDto.from(templateRepository.save(template));
  }

  /**
   * Find stock card template by facility type id and program id.
   *
   * @param programId      program id.
   * @param facilityTypeId facility type id.
   * @return the found template or null if not found.
   */
  public StockCardTemplateDto findByProgramIdAndFacilityTypeId(
          UUID programId, UUID facilityTypeId) {
    StockCardTemplate template = templateRepository
            .findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
    return StockCardTemplateDto.from(template);
  }

  /**
   * Get default stock card template, with all fields set to false.
   *
   * @return default stock card template.
   */
  public StockCardTemplateDto getDefaultStockCardTemplate() {
    List<StockCardFieldDto> cardFieldDtos =
            convertAllFieldsToDto(cardFieldsRepo, this::convertModelToDefaultDto);

    List<StockCardLineItemFieldDto> lineItemFieldDtos =
            convertAllFieldsToDto(lineItemFieldsRepo, this::convertModelToDefaultDto);

    StockCardTemplateDto dto = new StockCardTemplateDto();
    dto.setStockCardFields(cardFieldDtos);
    dto.setStockCardLineItemFields(lineItemFieldDtos);

    return dto;
  }

  private <F, T> List<T> convertAllFieldsToDto(
          PagingAndSortingRepository<F, UUID> repo, Function<F, T> converter) {
    return findAllFieldsFrom(repo)
            .map(converter)
            .collect(toList());
  }

  private <F> Stream<F> findAllFieldsFrom(PagingAndSortingRepository<F, UUID> repo) {
    return stream(repo.findAll().spliterator(), false);
  }

  private StockCardFieldDto convertModelToDefaultDto(
          AvailableStockCardFields model) {
    return new StockCardFieldDto(model.getName(), false, 0);
  }

  private StockCardLineItemFieldDto convertModelToDefaultDto(
          AvailableStockCardLineItemFields model) {
    return new StockCardLineItemFieldDto(model.getName(), false, 0);
  }

  private void checkProgramAndFacilityTypeExist(UUID programId, UUID facilityTypeId) {
    programReferenceDataService.findOne(programId);
    facilityTypeReferenceDataService.findOne(facilityTypeId);
  }
}
