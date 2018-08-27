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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

  private StockCardLineItemReason firstReason;
  private StockCardLineItemReason secondReason;

  @Override
  CrudRepository<StockCardLineItemReason, UUID> getRepository() {
    return reasonRepository;
  }

  @Before
  public void setUp() {
    firstReason = generateInstance();
    reasonRepository.save(firstReason);

    secondReason = StockCardLineItemReason
        .builder()
        .name("Name")
        .description("Description")
        .isFreeTextAllowed(true)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.DEBIT)
        .tags(Lists.newArrayList("newTag"))
        .build();
    reasonRepository.save(secondReason);
  }

  @Test
  public void shouldFindByReasonTypes() {
    List<StockCardLineItemReason> reasons =
        reasonRepository.findByReasonTypeIn(Arrays.asList(ReasonType.DEBIT, ReasonType.CREDIT));

    assertThat(reasons, hasItems(firstReason, secondReason));
  }

  @Test
  public void shouldFindByIds() {
    List<StockCardLineItemReason> reasons =
        reasonRepository.findByIdIn(Arrays.asList(firstReason.getId(), secondReason.getId()));

    assertThat(reasons.size(), is(2));
    assertThat(reasons, hasItems(firstReason, secondReason));
  }

  @Test
  public void shouldFindByName() {
    StockCardLineItemReason reason =
        reasonRepository.findByName(firstReason.getName());

    assertThat(reason, is(firstReason));
  }

  @Test
  public void shouldFindTags() {
    reasonRepository.deleteAll();

    Set<String> expected = generateReasonsWithTags();

    List<String> actual = reasonRepository.findTags();

    assertThat(actual, hasSize(expected.size()));
    assertThat(actual, containsInAnyOrder(expected.toArray(new String[0])));
  }

  @Test
  public void shouldCheckIfTagExists() {
    reasonRepository.deleteAll();

    generateReasonsWithTags().stream()
        .forEach(tag -> assertTrue(reasonRepository.existsByTag(tag)));
    assertFalse(reasonRepository.existsByTag("some-not-existing-tag"));
  }

  @Override
  StockCardLineItemReason generateInstance() {
    int instanceNumber = getNextInstanceNumber();
    return StockCardLineItemReason.builder()
        .name("Name" + instanceNumber)
        .description("Description" + instanceNumber)
        .isFreeTextAllowed(instanceNumber % 2 == 0)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .reasonType(ReasonType.CREDIT)
        .tags(createTags(instanceNumber))
        .build();
  }

  private List<String> createTags(int instanceNumber) {
    if (instanceNumber % 3 == 0 && instanceNumber % 5 == 0) {
      return Lists.newArrayList("FizzBuzz");
    } else if (instanceNumber % 5 == 0) {
      return Lists.newArrayList("Buzz");
    } else if (instanceNumber % 3 == 0) {
      return Lists.newArrayList("Fizz");
    } else {
      return Lists.newArrayList();
    }
  }

  private Set<String> generateReasonsWithTags() {
    Set<String> expected = Sets.newHashSet();

    for (int i = 0; i < 30; ++i) {
      StockCardLineItemReason entity = generateInstance();
      expected.addAll(entity.getTags());

      reasonRepository.save(entity);
    }

    return expected;
  }
}
