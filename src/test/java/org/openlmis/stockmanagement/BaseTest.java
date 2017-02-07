package org.openlmis.stockmanagement;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = {"classpath:application.properties", "classpath:test.properties"})
public abstract class BaseTest {
}
