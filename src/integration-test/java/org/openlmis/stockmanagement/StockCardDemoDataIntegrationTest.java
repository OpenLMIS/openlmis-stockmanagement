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

package org.openlmis.stockmanagement;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.openlmis.stockmanagement.util.Resource2Db;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;

public class StockCardDemoDataIntegrationTest {

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private JdbcTemplate template;

  @Autowired
  private AutowireCapableBeanFactory autowireCapableBeanFactory;

  @Test
  public void demoDataTest() throws Exception {
    TestDataInitializer initializer = autowireCapableBeanFactory.createBean(
        TestDataInitializer.class);
    initializer.run();

    Pageable pageable = new PageRequest(0, 1, Direction.ASC, "id");

    while (true) {
      Page<StockCard> page = stockCardRepository.findAll(pageable);

      if (null == page || !page.hasContent()) {
        break;
      }

      page.forEach(StockCard::calculateStockOnHand);

      pageable = pageable.next();
    }

    stockCardRepository.findAll();
  }
}
