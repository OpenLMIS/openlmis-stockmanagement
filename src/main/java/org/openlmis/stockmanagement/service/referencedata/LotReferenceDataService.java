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

import static java.util.Collections.emptyList;

import org.openlmis.stockmanagement.dto.LotDto;
import org.openlmis.stockmanagement.service.DataRetrievalException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

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
   * @return found page of lots.
   */
  public Page<LotDto> search(UUID tradeItemId) {
    HashMap<String, Object> params = new HashMap<>();
    params.put("tradeIdemId", tradeItemId);
    try {
      return getPage("search", params);
    } catch (DataRetrievalException ex) {
      return new PageImpl<>(emptyList());
    }
  }
}
