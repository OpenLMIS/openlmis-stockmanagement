package org.openlmis.template;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class CustomWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {

  @Value("${service.url}")
  private String serviceUrl;

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/docs").setViewName("redirect:" + serviceUrl + "/docs/");
    registry.addViewController("/docs/").setViewName("forward:/docs/index.html");
    super.addViewControllers(registry);
  }
}
