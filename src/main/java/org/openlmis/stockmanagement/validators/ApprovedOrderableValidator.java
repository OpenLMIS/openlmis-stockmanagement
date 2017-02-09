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

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_ORDERABLE_NOT_FOUND;

@Component
public class ApprovedOrderableValidator {

  @Autowired
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  public void validate(StockEventDto stockEventDto) {
    Collection<ApprovedProductDto> approvedProducts =
        approvedProductReferenceDataService.getApprovedProducts(
            stockEventDto.getFacilityId(), stockEventDto.getProgramId(), true);

    boolean isFoundInApprovedList = approvedProducts
        .stream()
        .anyMatch(product -> {
          UUID orderableId = product.getProgramOrderable().getOrderableId();
          return orderableId.equals(stockEventDto.getOrderableId());
        });

    if (!isFoundInApprovedList) {
      throw new ValidationMessageException(
          new Message(ERROR_ORDERABLE_NOT_FOUND, stockEventDto.getOrderableId()));
    }

  }
}
