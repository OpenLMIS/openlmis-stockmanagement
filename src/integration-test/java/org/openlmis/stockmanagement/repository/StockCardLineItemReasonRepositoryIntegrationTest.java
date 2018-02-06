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

package org.openlmis.stockmanagement.repository;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class StockCardLineItemReasonRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<StockCardLineItemReason> {

  @Autowired
  private StockCardLineItemReasonRepository reasonRepository;

  @Override
  CrudRepository<StockCardLineItemReason, UUID> getRepository() {
    return reasonRepository;
  }

  private StockCardLineItemReason firstReason;
  private StockCardLineItemReason secondReason;

  @Before
  public void setUp() throws Exception {
    reasonRepository.deleteAll();

    firstReason = generateInstance();
    reasonRepository.save(firstReason);

    secondReason = StockCardLineItemReason
        .builder()
        .name("Name")
        .description("Description")
        .isFreeTextAllowed(true)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.DEBIT)
        .build();
    reasonRepository.save(secondReason);
  }

  @Test
  public void shouldFindByReasonTypes() throws Exception {
    List<StockCardLineItemReason> reasons =
        reasonRepository.findByReasonTypeIn(Arrays.asList(ReasonType.DEBIT, ReasonType.CREDIT));

    assertThat(reasons.size(), is(2));
    assertThat(reasons, hasItems(firstReason, secondReason));
  }

  @Test
  public void shouldFindByIds() throws Exception {
    List<StockCardLineItemReason> reasons =
        reasonRepository.findByIdIn(Arrays.asList(firstReason.getId(), secondReason.getId()));

    assertThat(reasons.size(), is(2));
    assertThat(reasons, hasItems(firstReason, secondReason));
  }

  @Test
  public void shouldFindByName() throws Exception {
    StockCardLineItemReason reason =
        reasonRepository.findByName(firstReason.getName());

    assertThat(reason, is(firstReason));
  }

  @Override
  StockCardLineItemReason generateInstance() throws Exception {
    int instanceNumber = getNextInstanceNumber();
    return StockCardLineItemReason.builder()
        .name("Name" + instanceNumber)
        .description("Description" + instanceNumber)
        .isFreeTextAllowed(instanceNumber % 2 == 0)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.CREDIT)
        .build();
  }

}
