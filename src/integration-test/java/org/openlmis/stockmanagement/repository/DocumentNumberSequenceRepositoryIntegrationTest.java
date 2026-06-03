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
import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.stockmanagement.domain.event.DocumentNumberSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class DocumentNumberSequenceRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<DocumentNumberSequence> {

  @Autowired
  private DocumentNumberSequenceRepository documentNumberSequenceRepository;

  @Override
  CrudRepository<DocumentNumberSequence, UUID> getRepository() {
    return documentNumberSequenceRepository;
  }

  @Override
  DocumentNumberSequence generateInstance() {
    DocumentNumberSequence sequence = new DocumentNumberSequence();
    sequence.setFacilityId(randomUUID());
    sequence.setYear(2026);
    sequence.setMonth(getNextInstanceNumber());
    sequence.setLastSequenceNumber(0);
    return sequence;
  }

  @Test
  public void shouldReturnOneOnFirstCallForNewFacilityYearMonth() {
    UUID facilityId = randomUUID();

    int result = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5);

    assertEquals(1, result);
  }

  @Test
  public void shouldIncrementSequenceOnSubsequentCalls() {
    UUID facilityId = randomUUID();

    assertEquals(1, documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5));
    assertEquals(2, documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5));
    assertEquals(3, documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5));
  }

  @Test
  public void shouldKeepSeparateCountersPerMonth() {
    UUID facilityId = randomUUID();

    int may1 = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5);
    int may2 = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5);
    int june1 = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 6);
    int june2 = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 6);

    assertEquals(1, may1);
    assertEquals(2, may2);
    assertEquals(1, june1);
    assertEquals(2, june2);
  }

  @Test
  public void shouldKeepSeparateCountersPerYear() {
    UUID facilityId = randomUUID();

    int year2025 = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2025, 12);
    int year2026 = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 1);
    int year2027 = documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2027, 1);

    assertEquals(1, year2025);
    assertEquals(1, year2026);
    assertEquals(1, year2027);
  }

  @Test
  public void shouldKeepSeparateCountersPerFacility() {
    UUID facilityA = randomUUID();
    UUID facilityB = randomUUID();

    int a1 = documentNumberSequenceRepository.nextSequenceNumber(facilityA, 2026, 5);
    int a2 = documentNumberSequenceRepository.nextSequenceNumber(facilityA, 2026, 5);
    int b1 = documentNumberSequenceRepository.nextSequenceNumber(facilityB, 2026, 5);

    assertEquals(1, a1);
    assertEquals(2, a2);
    assertEquals(1, b1);
  }

  @Test
  public void shouldContinueIncrementingPastFourDigits() {
    UUID facilityId = randomUUID();

    DocumentNumberSequence preset = new DocumentNumberSequence();
    preset.setFacilityId(facilityId);
    preset.setYear(2026);
    preset.setMonth(5);
    preset.setLastSequenceNumber(9999);
    documentNumberSequenceRepository.save(preset);

    assertEquals(10000, documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5));
    assertEquals(10001, documentNumberSequenceRepository.nextSequenceNumber(facilityId, 2026, 5));
  }
}
