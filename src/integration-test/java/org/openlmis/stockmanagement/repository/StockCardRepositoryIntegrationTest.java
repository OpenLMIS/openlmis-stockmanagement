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

import com.google.common.collect.Maps;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.testutils.StockCardDataBuilder;
import org.openlmis.stockmanagement.testutils.StockCardLineItemDataBuilder;
import org.openlmis.stockmanagement.testutils.StockEventDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

public class StockCardRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<StockCard> {

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private StockCardRepository stockCardRepository;

  @Autowired
  private StockEventsRepository stockEventsRepository;

  @Override
  CrudRepository<StockCard, UUID> getRepository() {
    return stockCardRepository;
  }

  @Override
  StockCard generateInstance() throws Exception {
    return generateInstance(randomUUID(), randomUUID(), randomUUID(), randomUUID());
  }

  private StockCard generateInstance(UUID facility, UUID program, UUID product, UUID lot) {
    StockEvent event = new StockEventDataBuilder()
        .withoutId()
        .withFacility(facility)
        .withProgram(program)
        .build();

    event = stockEventsRepository.save(event);

    StockCardLineItem lineItem = new StockCardLineItemDataBuilder()
        .withoutId()
        .withOriginEvent(event)
        .build();

    StockCard stockCard = new StockCardDataBuilder(event)
        .withoutId()
        .withOrderable(product)
        .withLot(lot)
        .withLineItem(lineItem)
        .build();
    lineItem.setStockCard(stockCard);

    return stockCard;
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowCreatingCardForTheSameFacilityProgramProductAndLot() throws Exception {
    StockCard one = generateInstance();
    StockCard two = generateInstance(
        one.getFacilityId(), one.getProgramId(), one.getOrderableId(), one.getLotId()
    );

    stockCardRepository.save(one);
    stockCardRepository.save(two);

    entityManager.flush();
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowCreatingCardForTheSameFacilityProgramAndProduct() throws Exception {
    StockCard one = generateInstance();
    one.setLotId(null);

    StockCard two = generateInstance(
        one.getFacilityId(), one.getProgramId(), one.getOrderableId(), one.getLotId()
    );

    stockCardRepository.save(one);
    stockCardRepository.save(two);

    entityManager.flush();
  }

  @Test
  public void shouldPersistWithNullExtraData() throws Exception {
    StockCard one = generateInstance();
    one.getLineItems().get(0).setExtraData(null);

    stockCardRepository.save(one);

    entityManager.flush();
  }

  @Test
  public void shouldPersistWithEmptyExtraData() throws Exception {
    StockCard one = generateInstance();
    one.getLineItems().get(0).setExtraData(Maps.newHashMap());

    stockCardRepository.save(one);

    entityManager.flush();
  }

  @Test
  public void shouldGetStockCardFor() throws Exception {





    StockCard one = generateInstance();
    one.getLineItems().get(0).setExtraData(Maps.newHashMap());

    stockCardRepository.save(one);

    entityManager.flush();
  }
}
