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

package org.openlmis.stockmanagement.util.deferredloading;

import java.util.List;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.i18n.MessageKeys;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.util.Message;

public class LotByCodeDeferredLoader
    extends DeferredLoader<LotDto, String, LotByCodeDeferredLoader.Handle> {

  private final LotReferenceDataService lotReferenceDataService;

  public LotByCodeDeferredLoader(LotReferenceDataService lotReferenceDataService) {
    this.lotReferenceDataService = lotReferenceDataService;
  }

  @Override
  protected Handle newHandle(String key) {
    return new Handle(key);
  }

  @Override
  public void loadDeferredObjects() {
    final List<LotDto> allDeferredLots =
        lotReferenceDataService.findByExactCodes(deferredObjects.keySet());

    for (LotDto lot : allDeferredLots) {
      deferredObjects.remove(lot.getLotCode()).set(lot);
    }

    if (deferredObjects.size() > 0) {
      throw new ValidationMessageException(new Message(MessageKeys.ERROR_LOTS_NOT_FOUND,
          String.join(", ", deferredObjects.keySet())));
    }
  }

  public static class Handle extends DeferredObject<LotDto, String> {
    public Handle(String objectKey) {
      super(objectKey);
    }
  }
}
