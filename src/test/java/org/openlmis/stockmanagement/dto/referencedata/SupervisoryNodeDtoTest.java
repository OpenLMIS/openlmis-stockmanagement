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

package org.openlmis.stockmanagement.dto.referencedata;

import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import org.openlmis.stockmanagement.testutils.ObjectGenerator;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class SupervisoryNodeDtoTest {

  @Test
  public void equalsContract() {
    List<ObjectReferenceDto> list = ObjectGenerator.of(ObjectReferenceDto.class, 2);

    EqualsVerifier
        .forClass(SupervisoryNodeDto.class)
        .withRedefinedSuperclass()
        .withPrefabValues(ObjectReferenceDto.class, list.get(0), list.get(1))
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    SupervisoryNodeDto dto = new SupervisoryNodeDto();
    ToStringTestUtils.verify(SupervisoryNodeDto.class, dto);
  }

}
