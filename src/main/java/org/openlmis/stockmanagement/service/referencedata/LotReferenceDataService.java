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

import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  public List<LotDto> getAllLotsOf(UUID tradeItemId) {
    List<LotDto> allLots = new ArrayList<>();

    int pageNumber = 0;
    boolean isLastPage = false;

    while (!isLastPage) {
      Page<LotDto> onePage = getOnePage(tradeItemId, pageNumber);
      allLots.addAll(onePage.getContent());

      pageNumber++;
      isLastPage = onePage.isLast();
    }

    return allLots;
  }

  private Page<LotDto> getOnePage(UUID tradeItemId, int pageNumber) {
    HashMap<String, Object> params = new HashMap<>();
    params.put("tradeItemId", tradeItemId);
    params.put("page", pageNumber);
    return getPage("search", params);
  }
}
