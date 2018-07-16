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

import static java.util.UUID.randomUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import javax.transaction.Transactional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.BaseIntegrationTest;
import org.openlmis.stockmanagement.dto.PhysicalInventoryDto;
import org.openlmis.stockmanagement.dto.PhysicalInventoryLineItemDto;
import org.openlmis.stockmanagement.repository.PhysicalInventoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class PhysicalInventoryServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private PhysicalInventoryService physicalInventoryService;

  @Autowired
  private PhysicalInventoriesRepository physicalInventoriesRepository;

  private PhysicalInventoryLineItemDto lineItemDto;

  @Before
  public void setUp() throws Exception {
    mockAuthentication();
  }

  @After
  public void tearDown() throws Exception {
    physicalInventoriesRepository.deleteAll();
  }

  @Test
  public void shouldRetainUuidWhenSubmittingNewPhysicalInventory() throws Exception {
    PhysicalInventoryDto dto = newInventoryForSubmit();

    UUID uuid = dto.getId();
    Assert.assertNotNull(uuid);
    Assert.assertFalse(physicalInventoriesRepository.exists(uuid));

    physicalInventoryService.submitPhysicalInventory(dto, null);

    Assert.assertTrue(physicalInventoriesRepository.exists(uuid));
  }

  private PhysicalInventoryDto newInventoryForSubmit() {
    PhysicalInventoryDto inventory = new PhysicalInventoryDto();
    inventory.setId(randomUUID());
    inventory.setProgramId(randomUUID());
    inventory.setFacilityId(randomUUID());
    inventory.setIsDraft(true);
    lineItemDto = generateLineItem();
    inventory.setLineItems(Collections.singletonList(lineItemDto));
    return inventory;
  }

  private PhysicalInventoryLineItemDto generateLineItem() {
    return PhysicalInventoryLineItemDto
        .builder()
        .orderableId(UUID.randomUUID())
        .lotId(UUID.randomUUID())
        .quantity(5)
        .stockAdjustments(new ArrayList<>())
        .build();
  }
}
