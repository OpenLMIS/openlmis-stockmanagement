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

package org.openlmis.stockmanagement.web;

import static java.util.stream.Collectors.toSet;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DATE_WRONG_FORMAT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_UUID_WRONG_FORMAT;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.UuidUtil;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@NoArgsConstructor
public class SearchParams {

  private static final String PAGE = "page";
  private static final String SIZE = "size";
  private static final String SORT = "sort";
  private static final String ACCESS_TOKEN = "access_token";

  private MultiValueMap<String, String> params;

  /**
   * Constructs new SearchParams object from {@code MultiValueMap}.
   */
  public SearchParams(MultiValueMap<String, String> queryMap) {
    if (queryMap != null) {
      params = new LinkedMultiValueMap<>(queryMap);
      params.remove(PAGE);
      params.remove(SIZE);
      params.remove(SORT);
      params.remove(ACCESS_TOKEN);
    } else {
      params = new LinkedMultiValueMap<>();
    }
  }

  public boolean containsKey(String key) {
    return params.containsKey(key);
  }

  public String getFirst(String key) {
    return params.getFirst(key);
  }

  public Collection<String> get(String key) {
    return params.get(key);
  }

  public LinkedMultiValueMap<String, String> asMultiValueMap() {
    return new LinkedMultiValueMap<>(params);
  }

  public Collection<String> keySet() {
    return params.keySet();
  }

  public boolean isEmpty() {
    return MapUtils.isEmpty(params);
  }

  /**
   * Parses String value into {@link LocalDate}.
   * If format is wrong {@link ValidationMessageException} will be thrown.
   *
   * @param key key for value be parsed into LocalDate
   * @return parsed local date
   */
  public LocalDate getLocalDate(String key) {
    String value = getFirst(key);

    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException cause) {
      throw new ValidationMessageException(cause, new Message(ERROR_DATE_WRONG_FORMAT, value, key));
    }
  }

  /**
   * Parses String value into {@link UUID} based on given key.
   * If format is wrong {@link ValidationMessageException} will be thrown.
   *
   * @param key key for value be parsed into UUID
   * @return parsed UUID
   */
  public UUID getUuid(String key) {
    String value = getFirst(key);
    return UuidUtil.fromString(value)
        .orElseThrow(() ->
            new ValidationMessageException(new Message(ERROR_UUID_WRONG_FORMAT, value, key)));
  }

  /**
   * Parses String value into {@link UUID} based on given key.
   * If format is wrong {@link ValidationMessageException} will be thrown.
   *
   * @param key key for value be parsed into UUID
   * @return parsed list of UUIDs
   */
  public Set<UUID> getUuids(String key) {
    Collection<String> values = get(key);

    return values.stream()
        .map(value -> UuidUtil.fromString(value)
            .orElseThrow(() ->
                new ValidationMessageException(new Message(ERROR_UUID_WRONG_FORMAT, value, key))))
        .collect(toSet());
  }
}
