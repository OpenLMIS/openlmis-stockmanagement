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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.TestDataInitializer.BASE_DATA;
import static org.openlmis.stockmanagement.TestDataInitializer.DEMO_DATA;
import static org.openlmis.stockmanagement.TestDataInitializer.EDGE_CASE_DATA;
import static org.openlmis.stockmanagement.TestDataInitializer.FUNCTIONAL_TEST_DATA;
import static org.openlmis.stockmanagement.TestDataInitializer.LAYERS;
import static org.openlmis.stockmanagement.TestDataInitializer.PERFORMANCE_DATA;
import static org.openlmis.stockmanagement.TestDataInitializer.TABLES;
import static org.openlmis.stockmanagement.TestDataInitializer.location;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.util.Resource2Db;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.Silent.class)
public class TestDataInitializerTest {

  private static final String FIRST_TABLE = TABLES.get(0);
  private static final String SECOND_TABLE = TABLES.get(1);
  private static final String THIRD_TABLE = TABLES.get(2);
  private static final String REFRESH_DB = "refresh-db";
  private static final Pattern UUID_PATTERN = Pattern.compile(
      "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  @Mock
  private Resource2Db loader;

  @Mock
  private ResourceLoader resourceLoader;

  @Mock
  private Environment environment;

  private final Map<String, Resource> resources = new HashMap<>();

  private TestDataInitializer initializer;

  @Before
  public void setUp() {
    when(resourceLoader.getResource(anyString())).thenAnswer(invocation ->
        resources.computeIfAbsent(invocation.getArgument(0), this::resource));
    initializer = new TestDataInitializer(loader, resourceLoader, environment);
  }

  @Test
  public void shouldLoadEveryTableOfTheCurrentDemoDataInOrder() throws IOException {
    activeProfiles(DEMO_DATA);
    for (String table : TABLES) {
      existing(DEMO_DATA, table);
    }

    initializer.run();

    InOrder order = inOrder(loader);
    for (String table : TABLES) {
      order.verify(loader).insertToDbFromCsv(table, resources.get(location(DEMO_DATA, table)));
    }
  }

  @Test
  public void shouldLoadBaseLayerBeforeTheSelectedLayer() throws IOException {
    activeProfiles(DEMO_DATA);
    Resource baseThird = existing(BASE_DATA, THIRD_TABLE);
    Resource demoFirst = existing(DEMO_DATA, FIRST_TABLE);

    initializer.run();

    InOrder order = inOrder(loader);
    order.verify(loader).insertToDbFromCsv(THIRD_TABLE, baseThird);
    order.verify(loader).insertToDbFromCsv(FIRST_TABLE, demoFirst);
  }

  @Test
  public void shouldSkipTablesWithoutFile() throws IOException {
    activeProfiles(DEMO_DATA);
    Resource demoSecond = existing(DEMO_DATA, SECOND_TABLE);

    initializer.run();

    verify(loader).insertToDbFromCsv(SECOND_TABLE, demoSecond);
    verify(loader).insertToDbFromCsv(anyString(), any(Resource.class));
  }

  @Test
  public void shouldNotLoadLayersWhoseProfileIsInactive() throws IOException {
    activeProfiles(BASE_DATA, REFRESH_DB);
    final Resource baseSecond = existing(BASE_DATA, SECOND_TABLE);
    final Resource demoSecond = existing(DEMO_DATA, SECOND_TABLE);
    final Resource performanceSecond = existing(PERFORMANCE_DATA, SECOND_TABLE);

    initializer.run();

    verify(loader).insertToDbFromCsv(SECOND_TABLE, baseSecond);
    verify(loader, never()).insertToDbFromCsv(SECOND_TABLE, demoSecond);
    verify(loader, never()).insertToDbFromCsv(SECOND_TABLE, performanceSecond);
  }

  @Test
  public void shouldResolveBaseLayerForEveryDataProfile() {
    for (String layer : LAYERS) {
      List<String> resolved = TestDataInitializer.resolveLayers(layer);
      assertEquals(BASE_DATA, resolved.get(0));
      assertTrue(resolved.contains(layer));
    }
  }

  @Test
  public void shouldResolveLayersInLoadOrderRegardlessOfProfileOrder() {
    assertEquals(Arrays.asList(BASE_DATA, DEMO_DATA, EDGE_CASE_DATA, PERFORMANCE_DATA),
        TestDataInitializer.resolveLayers(
            PERFORMANCE_DATA, REFRESH_DB, EDGE_CASE_DATA, DEMO_DATA, BASE_DATA));
  }

  @Test
  public void shouldRunForEveryDataProfileButNotInTestRuns() {
    Profiles condition = Profiles.of(
        TestDataInitializer.class.getAnnotation(Profile.class).value());

    for (String layer : LAYERS) {
      assertTrue(layer, condition.matches(active(layer, REFRESH_DB)));
      assertFalse(layer, condition.matches(active(layer, "test-run")));
    }
    assertFalse(condition.matches(active(REFRESH_DB, "production")));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldRejectDemoAndFunctionalTestDataTogether() {
    TestDataInitializer.resolveLayers(DEMO_DATA, FUNCTIONAL_TEST_DATA);
  }

  @Test
  public void shouldKnowTheTableOfEveryLayerFile() throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    List<String> unknown = new ArrayList<>();
    int files = 0;

    for (String layer : LAYERS) {
      for (Resource file : resolver.getResources("classpath*:db/" + layer + "/*.csv")) {
        files++;
        String table = file.getFilename().replaceFirst("\\.csv$", "");
        if (!TABLES.contains(table)) {
          unknown.add(layer + "/" + file.getFilename());
        }
      }
    }

    assertTrue("Expected data layer files on the classpath", files > 0);
    assertEquals("Files the loader would silently skip; add their tables to TABLES",
        Collections.emptyList(), unknown);
  }

  @Test
  public void shouldReferOnlyToRowsOfBaseOrOwnLayer() throws IOException {
    List<String> problems = new ArrayList<>();
    Map<String, String> owners = owners(problems);

    for (String layer : LAYERS) {
      if (!BASE_DATA.equals(layer)) {
        for (Resource file : layerFiles(layer)) {
          addReferencesToOtherLayers(layer, file, owners, problems);
        }
      }
    }

    assertEquals("Rows may refer only to rows of base-data or of their own layer",
        Collections.emptyList(), problems.subList(0, Math.min(20, problems.size())));
  }

  private static Map<String, String> owners(List<String> problems) throws IOException {
    Map<String, String> owners = new HashMap<>();
    for (String layer : LAYERS) {
      for (Resource file : layerFiles(layer)) {
        for (String id : ids(file)) {
          String previous = owners.put(id, layer);
          if (previous != null && !previous.equals(layer)) {
            problems.add(id + " is in both " + previous + " and " + layer);
          }
        }
      }
    }
    return owners;
  }

  private static void addReferencesToOtherLayers(String layer, Resource file,
      Map<String, String> owners, List<String> problems) throws IOException {
    Matcher matcher = UUID_PATTERN.matcher(read(file));
    while (matcher.find()) {
      String owner = owners.get(matcher.group().toLowerCase(Locale.ENGLISH));
      if (owner != null && !BASE_DATA.equals(owner) && !layer.equals(owner)) {
        problems.add(layer + "/" + file.getFilename() + " refers to " + matcher.group()
            + " of " + owner);
      }
    }
  }

  private static Resource[] layerFiles(String layer) throws IOException {
    return new PathMatchingResourcePatternResolver()
        .getResources("classpath*:db/" + layer + "/*.csv");
  }

  private static String read(Resource file) throws IOException {
    try (InputStream stream = new BOMInputStream(file.getInputStream())) {
      return IOUtils.toString(stream, StandardCharsets.UTF_8);
    }
  }

  private static List<String> ids(Resource file) throws IOException {
    List<String> ids = new ArrayList<>();
    try (CSVParser parser = CSVParser.parse(read(file), CSVFormat.DEFAULT.withHeader())) {
      if (parser.getHeaderMap().containsKey("id")) {
        for (CSVRecord record : parser) {
          ids.add(record.get("id").toLowerCase(Locale.ENGLISH));
        }
      }
    }
    return ids;
  }

  private static Predicate<String> active(String... profiles) {
    return Arrays.asList(profiles)::contains;
  }

  private void activeProfiles(String... profiles) {
    when(environment.getActiveProfiles()).thenReturn(profiles);
  }

  private Resource existing(String layer, String table) {
    Resource resource = resources.computeIfAbsent(location(layer, table), this::resource);
    when(resource.exists()).thenReturn(true);
    return resource;
  }

  private Resource resource(String location) {
    return mock(Resource.class, location);
  }
}
