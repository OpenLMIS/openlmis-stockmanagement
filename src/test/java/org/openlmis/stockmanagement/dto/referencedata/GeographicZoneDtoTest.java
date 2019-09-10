package org.openlmis.stockmanagement.dto.referencedata;

import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.stockmanagement.testutils.GeographicZoneDtoDataBuilder;
import org.openlmis.stockmanagement.testutils.ToStringTestUtils;

public class GeographicZoneDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(GeographicZoneDto.class)
        .withPrefabValues(GeographicZoneDto.class,
            new GeographicZoneDtoDataBuilder().withId(UUID.randomUUID()).build(),
            new GeographicZoneDtoDataBuilder().withId(UUID.randomUUID()).build())
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS) // DTO fields cannot be final
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    GeographicZoneDto geographicZoneDto = new GeographicZoneDtoDataBuilder().build();
    ToStringTestUtils.verify(GeographicZoneDto.class, geographicZoneDto);
  }

}
