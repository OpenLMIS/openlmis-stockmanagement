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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.repository.DocumentNumberSequenceRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentNumberGeneratorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private DocumentNumberSequenceRepository sequenceRepository;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @InjectMocks
  private DocumentNumberGenerator generator;

  private final UUID facilityId = UUID.randomUUID();

  @Before
  public void setUp() {
    LocalDate fixedDate = LocalDate.of(2026, 5, 28);
    Clock fixedClock = Clock.fixed(
        fixedDate.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
    ReflectionTestUtils.setField(generator, "clock", fixedClock);
  }

  @Test
  public void formatsNumberWithZeroPaddedMonthAndFourDigitSequence() {
    FacilityDto facility = FacilityDto.builder().id(facilityId).code("FAC001").build();
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facility);
    when(sequenceRepository.nextSequenceNumber(facilityId, 2026, 5)).thenReturn(1);

    String result = generator.generate(facilityId);

    assertEquals("2026-05-FAC001-0001", result);
  }

  @Test
  public void preservesMultiDigitSequenceBeyondFourDigits() {
    FacilityDto facility = FacilityDto.builder().id(facilityId).code("FAC001").build();
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facility);
    when(sequenceRepository.nextSequenceNumber(facilityId, 2026, 5)).thenReturn(10000);

    String result = generator.generate(facilityId);

    assertEquals("2026-05-FAC001-10000", result);
  }

  @Test
  public void requestsSequenceForCurrentYearAndMonthFromClock() {
    FacilityDto facility = FacilityDto.builder().id(facilityId).code("FAC001").build();
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facility);
    when(sequenceRepository.nextSequenceNumber(any(UUID.class), anyInt(), anyInt()))
        .thenReturn(42);

    generator.generate(facilityId);

    verify(sequenceRepository).nextSequenceNumber(eq(facilityId), eq(2026), eq(5));
  }

  @Test
  public void throwsResourceNotFoundExceptionWhenFacilityMissing() {
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(null);

    expectedException.expect(ResourceNotFoundException.class);
    generator.generate(facilityId);
  }
}
