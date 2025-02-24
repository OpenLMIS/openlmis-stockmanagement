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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class OrderableReferenceDataService
    extends BaseReferenceDataService<OrderableDto> {

  @Override
  protected String getUrl() {
    return "/api/orderables/";
  }

  @Override
  protected Class<OrderableDto> getResultClass() {
    return OrderableDto.class;
  }

  @Override
  protected Class<OrderableDto[]> getArrayResultClass() {
    return OrderableDto[].class;
  }

  /**
   * Finds orderables by their ids.
   *
   * @param ids ids to look for.
   * @return a page of orderables
   */
  public List<OrderableDto> findByIds(Collection<UUID> ids) {
    return CollectionUtils.isEmpty(ids)
        ? Collections.emptyList()
        : getPage(RequestParameters.init().set("id", ids)).getContent();
  }

  public List<OrderableDto> findAll() {
    return getPage(Collections.emptyMap()).getContent();
  }

  /**
   * Finds orderables by their exact codes.
   *
   * @param exactCodes exact codes to look for.
   * @return a page of orderables
   */
  public List<OrderableDto> findByExactCodes(Collection<String> exactCodes) {
    return CollectionUtils.isEmpty(exactCodes) ? Collections.emptyList() :
        getPage(RequestParameters.init().set("exactCode", exactCodes)).getContent();
  }
}
