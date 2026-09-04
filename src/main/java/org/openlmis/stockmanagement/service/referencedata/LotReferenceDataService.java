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
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.RequestParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpStatusCodeException;

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

  /**
   * Finds lot by their exact codes.
   *
   * @param exactCodes exact codes to look for.
   * @return a page of lots
   */
  public List<LotDto> findByExactCodes(Collection<String> exactCodes) {
    return CollectionUtils.isEmpty(exactCodes)
        ? Collections.emptyList()
        : getPage(RequestParameters.init().set("exactCode", exactCodes)).getContent();
  }

  /**
   * Creates a lot in the reference data service using the stockmanagement service account.
   * A client error ({@code 4xx}, for example a rejected or duplicate code) is translated into a
   * {@link ValidationMessageException} so it surfaces as a coherent stock-event error, while a
   * server error is left as a {@link DataRetrievalException}.
   *
   * @param lotDto the lot to create
   * @return the created lot, including its generated id
   */
  public LotDto create(LotDto lotDto) {
    try {
      return createResource(lotDto);
    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode().is4xxClientError()) {
        throw new ValidationMessageException(ex,
            new Message(MessageKeys.ERROR_EVENT_LOT_CREATION_FAILED, lotDto.getLotCode()));
      }
      throw buildDataRetrievalException(ex);
    }
  }
}
