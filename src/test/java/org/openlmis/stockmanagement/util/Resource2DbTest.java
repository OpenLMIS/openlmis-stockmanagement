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

package org.openlmis.stockmanagement.util;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class Resource2DbTest {

  @Mock
  private JdbcTemplate template;

  @InjectMocks
  private Resource2Db resource2Db;

  @Test
  public void updateDbFromSqlShouldCloseInputStream() throws IOException {
    // given
    Resource resource = mock(Resource.class);
    InputStream inputStream = spy(IOUtils.toInputStream("some data"));
    when(resource.getInputStream()).thenReturn(inputStream);
    when(template.batchUpdate(any(String.class))).thenReturn(new int[]{1});

    // when
    resource2Db.updateDbFromSql(resource);

    // then
    verify(inputStream, times(1)).close();
    assertFalse(resource.isOpen());
  }

  @Test
  public void insertToDbFromCsvShouldCloseInputStream() throws IOException {
    // given
    Resource resource = mock(Resource.class);
    InputStream inputStream = spy(IOUtils.toInputStream("some data"));
    when(resource.getInputStream()).thenReturn(inputStream);
    when(template.batchUpdate(any(String.class), any(List.class))).thenReturn(new int[]{1});

    // when
    resource2Db.insertToDbFromCsv("sometable", resource);

    // then
    verify(inputStream, times(1)).close();
    assertFalse(resource.isOpen());
  }

  @Test(expected = NullPointerException.class)
  public void resource2DbWithNullResourceShouldThrowExecption() {
    new Resource2Db(null);
  }

  @Test(expected = NullPointerException.class)
  public void updateDbFromSqlWithNullShouldThrowException() throws IOException {
    resource2Db.updateDbFromSql(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void insertToDbFromCsvWithBlankTableNameShouldThrowException() throws IOException {
    Resource resource = mock(Resource.class);
    resource2Db.insertToDbFromCsv("", resource);
  }

  @Test(expected = NullPointerException.class)
  public void insertToDbFromCsvWithNullTableNameShouldThrowException() throws IOException {
    Resource resource = mock(Resource.class);
    resource2Db.insertToDbFromCsv(null, resource);
  }

  @Test(expected = NullPointerException.class)
  public void insertToDbFromCsvWithNullResourceShouldThrowException() throws IOException {
    resource2Db.insertToDbFromCsv("test", null);
  }
}
