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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LotReferenceDataService extends BaseReferenceDataService<LotDto> {
  @Override
  protected String getUrl() {
    return "/api/lots/";
  }

  @Override
  protected Class<LotDto> getResultClass() {
    return LotDto.class;
  }

  @Override
  protected Class<LotDto[]> getArrayResultClass() {
    return LotDto[].class;
  }

  /**
   * Search for lots under a specific trade item.
   *
   * @param tradeItemId trade item id.
   * @return found list of lots.
   */
  public List<LotDto> getAllLotsOf(UUID tradeItemId) {
    return getAllLotsMatching(tradeItemId, null);
  }

  /**
   * Search for lots expiring on a certain date.
   *
   * @param expirationDate expiration date.
   * @return found list of lots.
   */
  public List<LotDto> getAllLotsExpiringOn(LocalDate expirationDate) {
    return getAllLotsMatching(null, expirationDate);
  }
  
  private List<LotDto> getAllLotsMatching(UUID tradeItemId, LocalDate expirationDate) {
    HashMap<String, Object> params = new HashMap<>();

    if (null != tradeItemId) {
      params.put("tradeItemId", tradeItemId);
    }
    if (null != expirationDate) {
      params.put("expirationDate", expirationDate);
    }

    return getPage(params).getContent();
  }

  /**
   * Find Lot by IDs.
   *
   * @param ids the ids, not null
   * @return the list of lots, never null
   */
  public List<LotDto> findByIds(Collection<UUID> ids) {
    return CollectionUtils.isEmpty(ids)
        ? Collections.emptyList()
        : getPage(RequestParameters.init().set("id", ids)).getContent();
  }
}
