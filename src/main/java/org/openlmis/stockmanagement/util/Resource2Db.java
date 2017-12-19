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

import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Spring oriented utility class to load data into a database.  When given Spring's
 * {@link JdbcTemplate}, an instance of this class is able to run SQL inserts/updates against the
 * attached datasource. It knows what SQL to run, or what data to load, based on Spring
 * {@link Resource}'s passed in.
 */
public class Resource2Db {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(Resource2Db.class);

  private final JdbcTemplate template;

  /**
   * new with given data connection
   * @param template the active {@link JdbcTemplate} to run SQL updates against.
   * @throws NullPointerException if template is null.
   */
  public Resource2Db(JdbcTemplate template) {
    Validate.notNull(template);
    this.template = template;
  }

  /**
   * Update the database from a Resource which has lines of SQL.  One SQL statement per line.
   * @param resource the Resource with SQL in lines.
   * @throws IOException if the Resource can't be used.
   * @throws NullPointerException if the resource is null.
   */
  public void updateDbFromSql(Resource resource) throws IOException {
    XLOGGER.entry(resource.getDescription());
    Validate.notNull(resource);
    List<String> sqlLines = resourceToStrings(resource);
    updateDbFromSqlStrings(sqlLines);
    XLOGGER.exit();
  }

  /**
   * Insert into the database (a table) from a Resource with CSV data.
   * @param tableName the name of the table (incl schema) to load the data into.
   * @param resource the Resource as a CSV, with a header, that has the data to load.
   * @throws IOException if the Resource can't be used.
   * @throws NullPointerException if any of the arguments are null
   * @throws IllegalArgumentException if the tableName is blank
   */
  public void insertToDbFromCsv(String tableName, Resource resource) throws IOException {
    XLOGGER.entry(tableName, resource);
    Validate.notBlank(tableName);
    Validate.notNull(resource);
    insertToDbFromBatchedPair(tableName, resourceCsvToBatchedPair(resource));
    XLOGGER.exit();
  }

  /*
   converts a Resource into a List of Strings - used when those strings are direct SQL
   */
  private List<String> resourceToStrings(final Resource resource) throws IOException {
    XLOGGER.entry(resource.getDescription());

    List<String> lines;
    try (InputStreamReader isReader = new InputStreamReader(resource.getInputStream())) {
      lines = new BufferedReader(isReader).lines().collect(Collectors.toList());
    }
    assert null != lines;

    XLOGGER.exit("SQL lines read: " + lines.size());
    return lines;
  }

  /*
   converts a Resource which is a CSV, into a Pair where Pair.left is the SQL column names,
   and Pair.right is the rows of data which go into those columns (each row is an array, the array
   matches the order of the columns
   */
  Pair<List<String>, List<Object[]>> resourceCsvToBatchedPair(final Resource resource)
      throws IOException {
    XLOGGER.entry(resource.getDescription());

    // parse CSV
    try (InputStreamReader isReader = new InputStreamReader(resource.getInputStream())) {
      CSVParser parser = CSVFormat.DEFAULT.withHeader().withNullString("").parse(isReader);

      // read header row
      MutablePair<List<String>, List<Object[]>> readData = new MutablePair<>();
      readData.setLeft( new ArrayList<>( parser.getHeaderMap().keySet() ) );
      XLOGGER.info("Read header: " + readData.getLeft() );

      // read data rows
      List<Object[]> rows = new ArrayList<>();
      for ( CSVRecord record : parser.getRecords() ) {
        if ( ! record.isConsistent() ) {
          throw new IllegalArgumentException("CSV record inconsistent: " + record);
        }

        List theRow = IteratorUtils.toList(record.iterator());
        rows.add( theRow.toArray() );
      }
      readData.setRight(rows);

      XLOGGER.exit("Records read: " + readData.getRight().size());
      return readData;
    }
  }

  /*
   runs the list of SQL strings directly on the database - could be insert / update
   */
  private void updateDbFromSqlStrings(final List<String> sqlLines) {
    XLOGGER.entry();

    if (CollectionUtils.isEmpty(sqlLines)) {
      return;
    }

    int[] updateCounts = template.batchUpdate(sqlLines.toArray(new String[sqlLines.size()]));
    XLOGGER.exit("Total db updates: " + Arrays.stream(updateCounts).sum());
  }

  /**
   * Inserts data into a single table.  Given the columns and a list of data to insert, will
   * run a batch update to insert it.
   * @param tableName the name of the table (including schema) to insert into.
   * @param dataWithHeader a pair where pair.left is an ordered list of column names and pair.right
   *                       is an array of rows to insert, where each row is similarly ordered as
   *                       the columns in pair.left.
   */
  public void insertToDbFromBatchedPair(String tableName,
                                        Pair<List<String>, List<Object[]>> dataWithHeader) {
    XLOGGER.entry(tableName);

    String columnDesc = dataWithHeader.getLeft()
        .stream()
        .collect(joining(","));
    String valueDesc = dataWithHeader.getLeft()
        .stream()
        .map(s -> "?")
        .collect((joining(",")));
    String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)",
        tableName,
        columnDesc,
        valueDesc);
    XLOGGER.info("Insert SQL: " + insertSql);

    List<Object[]> data = dataWithHeader.getRight();
    data.forEach(e -> XLOGGER.info(tableName + ": " + Arrays.toString(e)));
    int[] updateCount = template.batchUpdate(insertSql, data);

    XLOGGER.exit("Total " + tableName + " inserts: " + Arrays.stream(updateCount).sum());
  }
}
