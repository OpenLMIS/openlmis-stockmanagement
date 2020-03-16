/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2020 VillageReach
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Sort;

@JsonComponent
public class CustomSortSerializer extends JsonSerializer<Sort> {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(CustomSortSerializer.class);

  @Override
  public void serialize(Sort value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeStartArray();

    value.iterator().forEachRemaining(v -> {
      try {
        gen.writeObject(v);
      } catch (IOException e) {
        XLOGGER.warn("Could not serialize sort", e);
      }
    });

    gen.writeEndArray();
  }

  @Override
  public Class<Sort> handledType() {
    return Sort.class;
  }
}