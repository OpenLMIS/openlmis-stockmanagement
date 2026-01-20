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

package org.openlmis.stockmanagement.validators;

import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_IS_DUPLICATE;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.hibernate.jpa.TypedParameterValue;
import org.hibernate.type.PostgresUUIDType;
import org.hibernate.type.StringType;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.dto.StockEventLineItemDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemRepository;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.validators.StockEventValidator;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * This validator ensures that the stock event being sent in is not a duplicate, by
 * making sure that all line items are not duplicated from a previous event in the same day.
 */
@Component(value = "DuplicateValidator")
public class DuplicateTransactionValidator implements StockEventValidator {
  private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  @Autowired
  private StockCardLineItemRepository stockCardLineItemRepository;

  @Override
  public void validate(StockEventDto stockEventDto) {
    XLOGGER.entry(stockEventDto);
    Profiler profiler = new Profiler("DUPLICATE_VALIDATOR");
    profiler.setLogger(XLOGGER);

    if (!stockEventDto.hasLineItems()) {
      return;
    }
    validateNotDuplicate(stockEventDto);

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
  }

  private void validateNotDuplicate(StockEventDto stockEventDto) {
    int lineItemCount = stockEventDto.getLineItems().size();
    int duplicateCount = 0;
    for (StockEventLineItemDto lineItem : stockEventDto.getLineItems()) {
      boolean isDuplicate = checkDuplicate(lineItem, stockEventDto.getFacilityId());
      if (isDuplicate) {
        duplicateCount += 1;
      }
    }
    System.out.println("number of items: " + lineItemCount);
    System.out.println("number of duplicates: " + duplicateCount);
    if (duplicateCount == lineItemCount) {
      throw new ValidationMessageException(
              new Message(ERROR_EVENT_IS_DUPLICATE));
    }
  }

  private boolean checkDuplicate(
          StockEventLineItemDto stockEventLineItemDto, UUID facilityId) {
    TypedParameterValue lotId = new TypedParameterValue(
            PostgresUUIDType.INSTANCE, stockEventLineItemDto.getLotId());
    TypedParameterValue destinationId = new TypedParameterValue(
            PostgresUUIDType.INSTANCE, stockEventLineItemDto.getDestinationId());
    TypedParameterValue sourceId = new TypedParameterValue(
            PostgresUUIDType.INSTANCE, stockEventLineItemDto.getSourceId());
    TypedParameterValue reasonId = new TypedParameterValue(
            PostgresUUIDType.INSTANCE, stockEventLineItemDto.getReasonId());
    TypedParameterValue vvmStatus = new TypedParameterValue(
            StringType.INSTANCE, stockEventLineItemDto.getExtraData().get("vvmStatus"));

    boolean duplicatesExist =
            stockCardLineItemRepository.getByAllGivenFields(
                    facilityId, lotId,
                    stockEventLineItemDto.getOrderableId(),
                    destinationId, sourceId,
                    stockEventLineItemDto.getOccurredDate().format(formatter),
                    stockEventLineItemDto.getQuantity(),
                    reasonId,
                    vvmStatus);

    return duplicatesExist;
  }

}
