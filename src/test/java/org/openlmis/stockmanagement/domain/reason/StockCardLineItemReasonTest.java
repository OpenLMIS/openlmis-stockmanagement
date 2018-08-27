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

package org.openlmis.stockmanagement.domain.reason;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.stockmanagement.dto.StockCardLineItemReasonDto;
import org.openlmis.stockmanagement.testutils.StockCardLineItemReasonDataBuilder;

public class StockCardLineItemReasonTest {

  @Test
  public void shouldExportData() {
    StockCardLineItemReasonDto exporter = new StockCardLineItemReasonDto();
    StockCardLineItemReason reason = new StockCardLineItemReasonDataBuilder().build();

    reason.export(exporter);

    assertThat(exporter.getId(), is(reason.getId()));
    assertThat(exporter.getName(), is(reason.getName()));
    assertThat(exporter.getDescription(), is(reason.getDescription()));
    assertThat(exporter.getType(), is(reason.getReasonType().toString()));
    assertThat(exporter.getCategory(), is(reason.getReasonCategory().toString()));
    assertThat(exporter.getIsFreeTextAllowed(), is(reason.getIsFreeTextAllowed()));
  }

  @Test
  public void shouldImportData() {
    StockCardLineItemReason domain = new StockCardLineItemReasonDataBuilder().build();
    StockCardLineItemReasonDto importer = new StockCardLineItemReasonDto();
    domain.export(importer);

    StockCardLineItemReason reason = StockCardLineItemReason.newInstance(importer);

    assertThat(reason.getId(), is(importer.getId()));
    assertThat(reason.getName(), is(importer.getName()));
    assertThat(reason.getDescription(), is(importer.getDescription()));
    assertThat(reason.getReasonType().toString(), is(importer.getType()));
    assertThat(reason.getReasonCategory().toString(), is(importer.getCategory()));
    assertThat(reason.getIsFreeTextAllowed(), is(importer.getIsFreeTextAllowed()));
  }

}
