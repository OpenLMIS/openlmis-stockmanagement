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

package org.openlmis.stockmanagement.validators;


import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.StockEventDto;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class StockEventVvmValidatorTest {

  @Mock
  private VvmValidator vvmValidator;

  @InjectMocks
  private StockEventVvmValidator validator;

  @Test
  public void shouldCallVvmValidator() {
    // given
    StockEventDto stockEvent = new StockEventDto();
    stockEvent.setLineItems(Collections.emptyList());

    doNothing()
        .when(vvmValidator).validate(eq(stockEvent.getLineItems()), anyString(), eq(true));

    // when
    validator.validate(stockEvent);

    // then
    verify(vvmValidator, atLeastOnce())
        .validate(eq(stockEvent.getLineItems()), anyString(), eq(true));
  }
}
