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

package org.openlmis.stockmanagement.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.exception.ResourceNotFoundException;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class JasperReportServiceTest {
  @InjectMocks
  private JasperReportService jasperReportService;

  @Mock
  private StockCardService stockCardService;

  @Test(expected = ResourceNotFoundException.class)
  public void should_throw_resource_not_found_exception_when_stock_card_not_exists()
      throws Exception {
    //given
    UUID stockCardId = UUID.randomUUID();
    Mockito.when(stockCardService.findStockCardById(stockCardId)).thenReturn(null);

    //then
    jasperReportService.getStockCardReportView(stockCardId);
  }
}