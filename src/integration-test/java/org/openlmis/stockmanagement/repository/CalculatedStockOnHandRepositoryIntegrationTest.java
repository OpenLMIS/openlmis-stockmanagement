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

import static java.util.UUID.randomUUID;

import java.util.UUID;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.event.CalculatedStockOnHand;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.testutils.CalculatedStockOnHandDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class CalculatedStockOnHandRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<CalculatedStockOnHand> {

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Autowired
  private CalculatedStockOnHandRepository calculatedStockOnHandRepository;

  @Override
  CrudRepository<CalculatedStockOnHand, UUID> getRepository() {
    return calculatedStockOnHandRepository;
  }

  @Override
  CalculatedStockOnHand generateInstance() {
    return generateInstance(randomUUID(), randomUUID(), randomUUID(), randomUUID());
  }

  private CalculatedStockOnHand generateInstance(UUID facility, UUID program, UUID product,
                                                 UUID lot) {
    StockEvent event = new StockEventDataBuilder()
            .withoutId()
            .withFacility(facility)
            .withProgram(program)
            .build();

    event = stockEventsRepository.save(event);

    StockCard stockCard = new StockCardDataBuilder(event)
            .withoutId()
            .withOrderable(product)
            .withLot(lot)
            .build();

    stockCard = stockCardRepository.save(stockCard);

    return new CalculatedStockOnHandDataBuilder()
        .withoutId()
        .withStockCard(stockCard)
        .build();
  }
}
