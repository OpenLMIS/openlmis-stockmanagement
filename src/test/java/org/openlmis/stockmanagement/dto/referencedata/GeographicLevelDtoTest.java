package org.openlmis.stockmanagement.dto.referencedata;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.testutils.GeographicLevelDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class GeographicLevelDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(GeographicLevelDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    GeographicLevelDto geographicLevelDto = new GeographicLevelDtoDataBuilder().build();
    ToStringTestUtils.verify(GeographicLevelDto.class, geographicLevelDto);
  }
}
