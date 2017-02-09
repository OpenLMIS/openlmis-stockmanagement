package org.openlmis.stockmanagement.validators;

import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.stockmanagement.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

import static com.google.common.collect.Iterables.concat;
import static java.util.stream.StreamSupport.stream;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_ORDERABLE_NOT_FOUND;

@Component(value = "ApprovedOrderableValidator")
public class ApprovedOrderableValidator implements StockEventValidator {

  @Autowired
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  /**
   * Validate if the orderable in stock event is in the approved list.
   *
   * @param stockEventDto the event to be validated.
   */
  public void validate(StockEventDto stockEventDto) {
    UUID facility = stockEventDto.getFacilityId();
    UUID program = stockEventDto.getProgramId();

    if (facility == null || program == null) {
      return;
    }

    Collection<ApprovedProductDto> fullSupply = getApprovedList(facility, program, true);
    Collection<ApprovedProductDto> nonFullSupply = getApprovedList(facility, program, false);

    boolean isFoundInApprovedList = tryToFindInBothLists(stockEventDto, fullSupply, nonFullSupply);

    if (!isFoundInApprovedList) {
      throw new ValidationMessageException(
          new Message(ERROR_ORDERABLE_NOT_FOUND, stockEventDto.getOrderableId()));
    }
  }

  private boolean tryToFindInBothLists(StockEventDto stockEventDto,
                                       Collection<ApprovedProductDto> fullSupplyList,
                                       Collection<ApprovedProductDto> nonFullSupplyList) {
    return stream(concat(fullSupplyList, nonFullSupplyList).spliterator(), false)
        .anyMatch(product -> {
          UUID orderableId = product.getProgramOrderable().getOrderableId();
          return orderableId.equals(stockEventDto.getOrderableId());
        });
  }

  private Collection<ApprovedProductDto> getApprovedList(
      UUID facilityId, UUID programId, boolean fullSupply) {
    return approvedProductReferenceDataService
        .getApprovedProducts(facilityId, programId, fullSupply);
  }
}
