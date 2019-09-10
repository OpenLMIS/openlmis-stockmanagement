package org.openlmis.stockmanagement.dto.referencedata;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class FacilityTypeDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(FacilityTypeDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    FacilityTypeDto facilityTypeDto = new FacilityTypeDto();
    ToStringTestUtils.verify(FacilityTypeDto.class, facilityTypeDto);
  }
}
