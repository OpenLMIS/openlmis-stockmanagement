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

import static org.junit.Assert.assertNotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.transaction.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test-run")
public abstract class BaseCrudRepositoryIntegrationTest<T extends BaseEntity> {

  abstract CrudRepository<T, UUID> getRepository();

  /*
   * Generate a unique instance of given type.
   * @return generated instance
   */
  abstract T generateInstance() throws Exception;

  private AtomicInteger instanceNumber = new AtomicInteger(0);

  int getNextInstanceNumber() {
    return this.instanceNumber.incrementAndGet();
  }

  protected void assertInstance(T instance) {
    assertNotNull(instance.getId());
  }

  @Test
  public void shouldCreate() throws Exception {
    CrudRepository<T, UUID> repository = this.getRepository();

    T instance = this.generateInstance();
    Assert.assertNull(instance.getId());

    instance = repository.save(instance);
    assertInstance(instance);

    Assert.assertTrue(repository.existsById(instance.getId()));
  }

  @Test
  public void shouldFindOne() throws Exception {
    CrudRepository<T, UUID> repository = this.getRepository();

    T instance = this.generateInstance();

    instance = repository.save(instance);
    assertInstance(instance);

    UUID id = instance.getId();

    instance = repository.findById(id).orElse(null);
    assertInstance(instance);
    Assert.assertEquals(id, instance.getId());
  }

  @Test
  public void shouldDelete() throws Exception {
    CrudRepository<T, UUID> repository = this.getRepository();

    T instance = this.generateInstance();
    assertNotNull(instance);

    instance = repository.save(instance);
    assertInstance(instance);

    UUID id = instance.getId();

    repository.deleteById(id);
    Assert.assertFalse(repository.existsById(id));
  }
}
