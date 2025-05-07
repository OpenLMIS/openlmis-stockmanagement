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

package org.openlmis.stockmanagement;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ExportSchemaFlywayCallbackTest {
  
  private ExportSchemaFlywayCallback callback;

  @Mock
  private Context mockConnection;

  @Mock
  private Runtime mockRuntime;
  
  @Mock
  private Process proc;

  @Before
  public void setUp() throws IOException, InterruptedException {
    callback = new ExportSchemaFlywayCallback();
    PowerMockito.mockStatic(Runtime.class);
    when(Runtime.getRuntime()).thenReturn(mockRuntime);
    when(mockRuntime.exec(anyString())).thenReturn(proc);
    InputStream stubStdOut = IOUtils.toInputStream("Out test line", "UTF-8");
    when(proc.getInputStream()).thenReturn(stubStdOut);
    when(proc.waitFor()).thenReturn(0);
  }
  
  @Test
  public void afterMigrateShouldProcessStreams() {

    callback.handle(Event.AFTER_MIGRATE, mockConnection);
  }
}
