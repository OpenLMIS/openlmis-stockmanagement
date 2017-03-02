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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openlmis.stockmanagement.testutils.StockCardLineItemReasonBuilder.createReason;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.adjustment.StockCardLineItemReason;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockCardLineItemReasonServiceTest {
  @Autowired
  private StockCardLineItemReasonService reasonService;

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @After
  public void tearDown() throws Exception {
    reasonRepository.deleteAll();
  }

  @Test(expected = ValidationMessageException.class)
  public void should_throw_validation_exception_with_unavailable_reason_dto() throws Exception {
    //given
    StockCardLineItemReason reason = new StockCardLineItemReason();

    //when
    reasonService.saveOrUpdate(reason);
  }

  @Test
  public void should_save_reason_when_pass_null_value_validation() throws Exception {
    assertThat(reasonRepository.count(), is(0L));

    //when
    reasonService.saveOrUpdate(createReason());

    //then
    assertThat(reasonRepository.count(), is(1L));
  }

  @Test
  public void should_not_create_duplicate_reason() throws Exception {
    //given: there is an existing reason
    reasonService.saveOrUpdate(createReason());
    assertThat(reasonRepository.count(), is(1L));

    //when: try to create a reason with same name type category and isFreeTextAllowed
    StockCardLineItemReason newReason = createReason();
    reasonService.saveOrUpdate(newReason);

    //then:
    assertThat(reasonRepository.count(), is(1L));
  }

  @Test
  public void should_update_existing_reason() throws Exception {
    //given: there is an existing reason
    reasonService.saveOrUpdate(createReason());
    assertThat(reasonRepository.count(), is(1L));
    UUID savedReasonId = reasonRepository.findAll().iterator().next().getId();

    //when:
    StockCardLineItemReason newReason = createReason();
    newReason.setId(savedReasonId);
    newReason.setIsFreeTextAllowed(false);
    StockCardLineItemReason updatedReason = reasonService.saveOrUpdate(newReason);

    //then
    assertThat(updatedReason.getIsFreeTextAllowed(), is(false));
    assertThat(reasonRepository.count(), is(1L));
  }

}