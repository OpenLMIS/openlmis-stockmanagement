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

package org.openlmis.stockmanagement.testutils;

import org.openlmis.stockmanagement.dto.CanFulfillForMeEntryDto;
import org.openlmis.stockmanagement.dto.ObjectReferenceDto;
import java.time.ZonedDateTime;

public class CanFulfillForMeEntryDtoDataBuilder {

  private ObjectReferenceDto stockCard;
  private ObjectReferenceDto orderable;
  private ObjectReferenceDto lot;
  private Integer stockOnHand;
  private ZonedDateTime processedDate;

  /**
   * Creates builder for creating new instance of {@link CanFulfillForMeEntryDto}.
   */
  public CanFulfillForMeEntryDtoDataBuilder() {
    stockCard = new ObjectReferenceDtoDataBuilder().withPath("api/stockCards").build();
    orderable = new ObjectReferenceDtoDataBuilder().withPath("api/orderables").build();
    lot = new ObjectReferenceDtoDataBuilder().withPath("api/lots").build();
    stockOnHand = 10;
    processedDate = ZonedDateTime.now();
  }

  /**
   * Creates new instance of {@link CanFulfillForMeEntryDto} with properties.
   * @return created object reference.
   */
  public CanFulfillForMeEntryDto build() {
    return new CanFulfillForMeEntryDto(stockCard, orderable, lot, stockOnHand, processedDate);
  }
}
