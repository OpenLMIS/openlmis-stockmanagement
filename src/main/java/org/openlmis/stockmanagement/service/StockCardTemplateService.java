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

package org.openlmis.stockmanagement.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_FACILITY_TYPE_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_PROGRAM_ID_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_STOCK_CARD_FIELD_DUPLICATED;

import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.dto.StockCardFieldDto;
import org.openlmis.stockmanagement.dto.StockCardLineItemFieldDto;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.AvailableStockCardFieldsRepository;
import org.openlmis.stockmanagement.repository.AvailableStockCardLineItemFieldsRepository;
import org.openlmis.stockmanagement.repository.StockCardTemplatesRepository;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A service that wraps around repository to provide ability
 * to save or update stock card template.
 */
@Service
public class StockCardTemplateService {

  @Autowired
  private StockCardTemplatesRepository templateRepository;

  @Autowired
  private AvailableStockCardFieldsRepository cardFieldsRepo;

  @Autowired
  private AvailableStockCardLineItemFieldsRepository lineItemFieldsRepo;

  @Autowired
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  /**
   * Save or update stock card template by facility type id and program id.
   *
   * @param templateDto object to save or update.
   * @return the saved or updated object.
   */
  @Transactional
  public StockCardTemplateDto saveOrUpdate(StockCardTemplateDto templateDto) {
    checkProgramAndFacilityTypeIdNotNull(templateDto);
    checkFieldsDuplication(templateDto);

    StockCardTemplate template = templateDto.toModel(
        findAllFieldsFrom(cardFieldsRepo).collect(toList()),
        findAllFieldsFrom(lineItemFieldsRepo).collect(toList()));

    StockCardTemplate found = templateRepository.findByProgramIdAndFacilityTypeId(
        template.getProgramId(), template.getFacilityTypeId());

    if (found != null) {
      template.setId(found.getId());
      templateRepository.delete(found);
    } else {
      programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(
          template.getProgramId(), template.getFacilityTypeId());
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

  private void checkProgramAndFacilityTypeIdNotNull(StockCardTemplateDto templateDto) {
    if (templateDto.getProgramId() == null) {
      throw new ValidationMessageException(new Message(ERROR_PROGRAM_ID_MISSING));
    }
    if (templateDto.getFacilityTypeId() == null) {
      throw new ValidationMessageException(new Message(ERROR_FACILITY_TYPE_ID_MISSING));
    }
  }

  private void checkFieldsDuplication(StockCardTemplateDto templateDto) {
    List<StockCardFieldDto> cardFields = templateDto.getStockCardFields();
    long cardFieldCount = cardFields.stream().map(StockCardFieldDto::getName).distinct().count();

    List<StockCardLineItemFieldDto> lineItemFields = templateDto.getStockCardLineItemFields();
    long lineItemFieldCount = lineItemFields.stream()
        .map(StockCardLineItemFieldDto::getName).distinct().count();

    if (cardFieldCount < cardFields.size() || lineItemFieldCount < lineItemFields.size()) {
      throw new ValidationMessageException(new Message(ERROR_STOCK_CARD_FIELD_DUPLICATED));
    }
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
}
