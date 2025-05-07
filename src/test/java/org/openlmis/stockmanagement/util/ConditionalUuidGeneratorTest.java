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

package org.openlmis.stockmanagement.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.UUID;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.UUIDGenerationStrategy;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.BaseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConditionalUuidGeneratorTest {

  @Mock
  private SessionImplementor session;

  @Mock
  private UUIDTypeDescriptor.ValueTransformer valueTransformer;

  @Mock
  private UUIDGenerationStrategy strategy;

  @Mock
  private BaseEntity entity;

  private ConditionalUuidGenerator generator;

  @Before
  public void setUp() {
    generator = new ConditionalUuidGenerator();
    ReflectionTestUtils.setField(generator, "strategy", strategy);
    ReflectionTestUtils.setField(generator, "valueTransformer", valueTransformer);
  }

  @Test
  public void shouldReturnExistingId() {
    UUID expected = UUID.randomUUID();

    when(entity.getId()).thenReturn(expected);
    Serializable actual = generator.generate(session, entity);

    assertThat(actual)
        .isInstanceOf(UUID.class)
        .isEqualTo(expected);
  }

  @Test
  public void shouldGenerateNewId() {
    UUID expected = UUID.randomUUID();

    when(strategy.generateUUID(session)).thenReturn(expected);
    when(valueTransformer.transform(any(UUID.class)))
        .thenAnswer(args -> args.getArgument(0, UUID.class));

    when(entity.getId()).thenReturn(null);
    Serializable actual = generator.generate(session, entity);

    assertThat(actual)
        .isInstanceOf(UUID.class)
        .isEqualTo(expected);
  }
}
