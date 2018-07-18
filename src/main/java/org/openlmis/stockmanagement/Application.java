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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.collections4.MultiValuedMap;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.openlmis.stockmanagement.i18n.ExposedMessageSourceImpl;
import org.openlmis.stockmanagement.web.stockcardrangesummary.StockCardRangeSummaryController;
import org.openlmis.stockmanagement.web.stockcardrangesummary.StockCardRangeSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@SpringBootApplication
@ImportResource("applicationContext.xml")
@EnableAsync
public class Application {
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  @Value("${defaultLocale}")
  private Locale locale;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
   * Creates new LocaleResolver.
   *
   * @return Created LocalResolver.
   */
  @Bean
  public LocaleResolver localeResolver() {
    CookieLocaleResolver lr = new CookieLocaleResolver();
    lr.setCookieName("lang");
    lr.setDefaultLocale(locale);
    return lr;
  }

  /**
   * Creates new MessageSource.
   *
   * @return Created MessageSource.
   */
  @Bean
  public ExposedMessageSourceImpl messageSource() {
    ExposedMessageSourceImpl messageSource = new ExposedMessageSourceImpl();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setUseCodeAsDefaultMessage(true);
    return messageSource;
  }


  /**
   * Configures the Flyway migration strategy to clean the DB before migration first.  This is used
   * as the default unless the Spring Profile "production" is active.
   *
   * @return the clean-migrate strategy
   */
  @Bean
  @Profile("!production")
  public FlywayMigrationStrategy cleanMigrationStrategy() {
    return flyway -> {
      LOGGER.info("Using clean-migrate flyway strategy -- production profile not active");
      flyway.setCallbacks(flywayCallback());
      flyway.clean();
      flyway.migrate();
    };
  }

  @Bean
  public FlywayCallback flywayCallback() {
    return new ExportSchemaFlywayCallback();
  }

  /**
   * Configures the Flyway migration strategy to clean the DB before migration first.  This is used
   * as the default unless the Spring Profile "production" is active.
   */
  @Bean
  public CommandLineRunner commandLineRunner(StockCardRangeSummaryController controller) {
    return args -> {
      MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
      map.add("programId", "418bdc1d-c303-4bd0-b2d3-d8901150a983");
      map.add("facilityId", "c62dea9b-6974-4101-ba39-b09914165967");
      map.add("orderableId", "b61c652d-2259-41d7-8bb6-fc5fcdd95626");
      Pageable pageable = new PageRequest(0, 30);
      Page<StockCardRangeSummaryDto> dto = controller.getStockCardRangeSummaries(map, pageable);

      System.out.println(dto);
    };
  }
}