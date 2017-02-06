package org.openlmis.stockmanagement.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static java.sql.Timestamp.valueOf;
import static java.time.ZoneId.of;

/**
 * This class is a converter for the ZonedDateTime class with the database, to convert to/from a SQL
 * timestamp. To ensure a ZonedDateTime entity property uses a timezone, its Column annotation
 * should be annotated as such.
 */
@Converter(autoApply = true)
public class ZonedDateTimeAttributeConverter
        implements AttributeConverter<ZonedDateTime, Timestamp> {

  @Override
  public Timestamp convertToDatabaseColumn(ZonedDateTime entityValue) {
    return (entityValue == null) ? null :
            valueOf(entityValue.withZoneSameInstant(of("UTC")).toLocalDateTime());
  }

  @Override
  public ZonedDateTime convertToEntityAttribute(Timestamp databaseValue) {
    return (databaseValue == null) ? null : databaseValue.toLocalDateTime().atZone(
            of("UTC"));
  }
}