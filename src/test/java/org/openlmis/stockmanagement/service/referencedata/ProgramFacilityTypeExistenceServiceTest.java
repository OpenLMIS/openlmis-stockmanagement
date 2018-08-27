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

package org.openlmis.stockmanagement.service.referencedata;

import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.FacilityTypeDto;
import org.openlmis.stockmanagement.dto.referencedata.ProgramDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;

@RunWith(MockitoJUnitRunner.class)
public class ProgramFacilityTypeExistenceServiceTest {
  @InjectMocks
  ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Mock
  ProgramReferenceDataService programRefDataService;

  @Mock
  FacilityTypeReferenceDataService facilityTypeRefDataService;

  @Before
  public void setUp() throws Exception {
    //any program or facility type exist by default
    when(programRefDataService.findOne(any(UUID.class))).thenReturn(new ProgramDto());
    when(facilityTypeRefDataService.findOne(any(UUID.class))).thenReturn(new FacilityTypeDto());
  }

  @Test(expected = ValidationMessageException.class)
  public void throwValidationMessageExceptionWhenProgramNotFound() throws Exception {
    UUID facilityTypeId = randomUUID();
    UUID programId = randomUUID();
    when(programRefDataService.findOne(programId)).thenThrow(
        new ValidationMessageException("errorKey"));

    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);
  }

  @Test(expected = ValidationMessageException.class)
  public void throwValidationMessageExceptionWhenFacilityTypeNotFound() throws Exception {
    UUID facilityTypeId = randomUUID();
    UUID programId = randomUUID();
    when(facilityTypeRefDataService.findOne(facilityTypeId)).thenThrow(
        new ValidationMessageException("errorKey"));

    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);
  }

  @Test
  public void shouldNotThrowValidationMessageExceptionInHappyPath() throws Exception {
    UUID facilityTypeId = randomUUID();
    UUID programId = randomUUID();

    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);
  }
}