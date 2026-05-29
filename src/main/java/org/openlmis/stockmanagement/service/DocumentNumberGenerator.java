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

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.FacilityDto;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.repository.DocumentNumberSequenceRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentNumberGenerator {

  @Autowired
  private DocumentNumberSequenceRepository sequenceRepository;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private Clock clock;

  /**
   * Generates the next document number for the given facility in the form
   * {@code {YEAR}-{MONTH}-{FACILITY_CODE}-{SEQ}}. Year and month are derived from the
   * application clock (configured via {@code time.zoneId}), not the facility's local time.
   */
  public String generate(UUID facilityId) {
    FacilityDto facility = facilityReferenceDataService.findOne(facilityId);
    if (facility == null) {
      throw new ResourceNotFoundException(
          new Message(MessageKeys.ERROR_FACILITY_NOT_FOUND, facilityId));
    }

    LocalDate today = LocalDate.now(clock);
    int year = today.getYear();
    int month = today.getMonthValue();

    int sequence = sequenceRepository.nextSequenceNumber(facilityId, year, month);

    return String.format("%d-%02d-%s-%04d", year, month, facility.getCode(), sequence);
  }
}
