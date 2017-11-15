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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class StockCardLineItemReasonServiceTest {
  @InjectMocks
  private StockCardLineItemReasonService reasonService;

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_with_unavailable_reason_dto() throws Exception {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReason();

    //when
    reasonService.saveOrUpdate(reason);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_when_reason_id_not_found_in_db() throws Exception {
    //given
    UUID reasonId = UUID.randomUUID();
    when(reasonRepository.findOne(reasonId)).thenReturn(null);
    reasonService.checkUpdateReasonIdExists(reasonId);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_exception_when_creating_reason_name_is_duplicate_with_other_one()
      throws Exception {
    //given
    StockCardLineItemReason creatingReason = new StockCardLineItemReasonDataBuilder()
        .withoutId()
        .build();
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder().build();
    when(reasonRepository.findByName(creatingReason.getName())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(creatingReason);
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_exception_when_updating_reason_name_is_duplicate_with_other_one()
      throws Exception {
    //given
    StockCardLineItemReason updatingReason = new StockCardLineItemReasonDataBuilder().build();
    StockCardLineItemReason existingReason = new StockCardLineItemReasonDataBuilder().build();
    when(reasonRepository.findByName(updatingReason.getName())).thenReturn(existingReason);

    //when
    reasonService.saveOrUpdate(updatingReason);
  }

  @Test
  public void should_get_all_reasons_when_pass_validation() throws Exception {
    //given
    when(reasonRepository.findAll()).thenReturn(
        asList(new StockCardLineItemReasonDataBuilder().withName("test reason 1").build(),
            new StockCardLineItemReasonDataBuilder().withName("test reason 2").build(),
            new StockCardLineItemReasonDataBuilder().withName("test reason 3").build()));

    //when
    List<StockCardLineItemReason> reasons = reasonService.findReasons();

    //then
    assertThat(reasons.size(), is(3));
    assertThat(reasons.get(0).getName(), is("test reason 1"));
    assertThat(reasons.get(1).getName(), is("test reason 2"));
    assertThat(reasons.get(2).getName(), is("test reason 3"));
  }

  @Test
  public void should_save_reason_when_pass_null_value_validation() throws Exception {
    //when
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().withoutId().build();
    reasonService.saveOrUpdate(reason);

    //then
    verify(reasonRepository, times(1)).save(reason);
  }

}