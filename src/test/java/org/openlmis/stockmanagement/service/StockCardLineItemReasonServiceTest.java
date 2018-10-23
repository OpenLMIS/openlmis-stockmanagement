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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_CATEGORY_CHANGED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_CATEGORY_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_ID_NOT_FOUND;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_NAME_DUPLICATE;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_NAME_MISSING;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TYPE_CHANGED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_LINE_ITEM_REASON_TYPE_MISSING;

import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class StockCardLineItemReasonServiceTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @InjectMocks
  private StockCardLineItemReasonService reasonService;

  @Test
  public void shouldThrowValidationExceptionIfReasonDoesNotHaveName() {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder()
        .withName(null)
        .build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_NAME_MISSING);

    //when
    reasonService.saveOrUpdate(reason);
  }

  @Test
  public void shouldThrowValidationExceptionIfReasonDoesNotHaveType() {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder()
        .withReasonType(null)
        .build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_TYPE_MISSING);

    //when
    reasonService.saveOrUpdate(reason);
  }

  @Test
  public void shouldThrowValidationExceptionIfReasonDoesNotHaveCategory() {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder()
        .withoutCategory()
        .build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_CATEGORY_MISSING);

    //when
    reasonService.saveOrUpdate(reason);
  }

  @Test
  public void shouldThrowValidationExceptionIfReasonDoesNotHaveIsFreeTextAllowed() {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder()
        .withoutIsFreeTextAllowed()
        .build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_ISFREETEXTALLOWED_MISSING);

    //when
    reasonService.saveOrUpdate(reason);
  }

  @Test
  public void shouldThrowValidationExceptionWhenReasonIdNotFoundInDb() {
    //given
    UUID reasonId = UUID.randomUUID();

    when(reasonRepository.exists(reasonId)).thenReturn(false);
    reasonService.checkUpdateReasonIdExists(reasonId);
  }

  @Test
  public void shouldNotThrowValidationExceptionWhenReasonFoundInDb() {
    //given
    UUID reasonId = UUID.randomUUID();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_ID_NOT_FOUND);

    when(reasonRepository.exists(reasonId)).thenReturn(true);

    reasonService.checkUpdateReasonIdExists(reasonId);
  }

  @Test
  public void shouldThrowExceptionWhenCreatingReasonNameIsDuplicateWithOtherOne() {
    //given
    StockCardLineItemReason creatingReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .build();
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder().build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_NAME_DUPLICATE);

    when(reasonRepository.findByName(creatingReason.getName())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(creatingReason);
  }

  @Test
  public void shouldThrowExceptionWhenUpdatingReasonNameIsDuplicateWithOtherOne() {
    //given
    StockCardLineItemReason updatingReason = new StockCardLineItemReasonDataBuilder().build();
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder().build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_NAME_DUPLICATE);

    when(reasonRepository.findByName(updatingReason.getName())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(updatingReason);
  }

  @Test
  public void shouldNotThrowExceptionWhenUpdatingReasonNameIsNotDuplicate() {
    //given
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder()
        .build();
    StockCardLineItemReason updatingReason = new StockCardLineItemReasonDataBuilder()
        .withId(existingReason.getId())
        .withName(existingReason.getName())
        .build();

    when(reasonRepository.findByName(updatingReason.getName())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(updatingReason);

    //then
    verify(reasonRepository, times(1)).save(updatingReason);
  }

  @Test
  public void shouldSaveReasonWhenPassNullValueValidation() {
    //when
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().withoutId().build();
    reasonService.saveOrUpdate(reason);

    //then
    verify(reasonRepository, times(1)).save(reason);
  }

  @Test
  public void shouldNotThrowExceptionIfNewReasonWithIdIsAdded() {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().build();

    //when
    when(reasonRepository.findOne(reason.getId())).thenReturn(null);

    reasonService.saveOrUpdate(reason);

    //then
    verify(reasonRepository, times(1)).save(reason);
  }

  @Test
  public void shouldNotThrowExceptionIfReasonIsUpdatedAndInvariantsWhereNotChanged() {
    //given
    StockCardLineItemReason updatingReason = new StockCardLineItemReasonDataBuilder()
        .withDebitType()
        .withName("abc")
        .build();
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder()
        .withId(updatingReason.getId())
        .withDebitType()
        .withName("def")
        .build();

    when(reasonRepository.findOne(updatingReason.getId())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(updatingReason);

    //then
    verify(reasonRepository, times(1)).save(updatingReason);
  }

  @Test
  public void shouldThrowExceptionIfTypeWasChanged() {
    //given
    StockCardLineItemReason updatingReason = new StockCardLineItemReasonDataBuilder()
        .withDebitType()
        .build();
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder()
        .withId(updatingReason.getId())
        .withCreditType()
        .build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_TYPE_CHANGED);

    when(reasonRepository.findOne(updatingReason.getId())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(updatingReason);
  }

  @Test
  public void shouldThrowExceptionIfCategoryWasChanged() {
    //given
    StockCardLineItemReason updatingReason = new StockCardLineItemReasonDataBuilder()
        .withAdjustmentCategory()
        .build();
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder()
        .withId(updatingReason.getId())
        .withPhysicalInventoryCategory()
        .build();

    willThrowValidationMessageException(ERROR_LINE_ITEM_REASON_CATEGORY_CHANGED);

    when(reasonRepository.findOne(updatingReason.getId())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(updatingReason);
  }

  private void willThrowValidationMessageException(String message) {
    exception.expect(ValidationMessageException.class);
    exception.expectMessage(message);
  }
}
