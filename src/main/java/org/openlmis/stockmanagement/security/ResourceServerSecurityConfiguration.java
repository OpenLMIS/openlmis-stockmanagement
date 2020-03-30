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

package org.openlmis.stockmanagement.security;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@Import({MethodSecurityConfiguration.class})
@EnableResourceServer
public class ResourceServerSecurityConfiguration implements ResourceServerConfigurer {

  private TokenExtractor tokenExtractor = new BearerTokenExtractor();

  @Value("${auth.resourceId}")
  private String resourceId;

  @Value("${cors.allowedOrigins}")
  private String[] allowedOrigins;

  @Value("${cors.allowedMethods}")
  private String[] allowedMethods;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
    resources.resourceId(resourceId);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.addFilterAfter(new OncePerRequestFilter() {
      @Override
      protected void doFilterInternal(HttpServletRequest request,
                                      HttpServletResponse response, FilterChain filterChain)
              throws ServletException, IOException {
        // We don't want to allow access to a resource with no token so clear
        // the security context in case it is actually an OAuth2Authentication
        if (tokenExtractor.extract(request) == null) {
          SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
      }
    }, AbstractPreAuthenticatedProcessingFilter.class);
    http.csrf().disable();

    http
            .authorizeRequests()
            .antMatchers(
                    "/stockmanagement",
                    "/webjars/**",
                    "/stockmanagement/webjars/**",
                    "/stockmanagement/docs/**"
            ).permitAll()
            .antMatchers("/**").fullyAuthenticated();
  }

  /**
   * AccessTokenConverter bean initializer.
   */
  @Bean
  public AccessTokenConverter accessTokenConverter() {
    DefaultAccessTokenConverter defaultAccessTokenConverter = new DefaultAccessTokenConverter();
    defaultAccessTokenConverter.setUserTokenConverter(new CustomUserAuthenticationConverter());
    return defaultAccessTokenConverter;
  }

  /**
   * RemoteTokenServices bean initializer.
   *
   * @param checkTokenUrl url to check tokens against
   * @param clientId      client's id
   * @param clientSecret  client's secret
   * @return token services
   */
  @Bean
  @Autowired
  public RemoteTokenServices remoteTokenServices(
      @Value("${auth.server.url}") String checkTokenUrl,
      @Value("${auth.server.clientId}") String clientId,
      @Value("${auth.server.clientSecret}") String clientSecret,
      @Value("${auth.server.invalidToken.retryLimit}") int invalidTokenRetryLimit) {
    final RemoteTokenServices remoteTokenServices = new CustomTokenServices(invalidTokenRetryLimit);
    remoteTokenServices.setCheckTokenEndpointUrl(checkTokenUrl);
    remoteTokenServices.setClientId(clientId);
    remoteTokenServices.setClientSecret(clientSecret);
    remoteTokenServices.setAccessTokenConverter(accessTokenConverter());
    return remoteTokenServices;
  }

  /**
   * CorsConfigurationSource bean initializer.
   * @return cors configuration
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    if (allowedOrigins.length > 0) {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
      configuration.setAllowedMethods(Arrays.asList(allowedMethods));
      source.registerCorsConfiguration("/**", configuration);
    }
    return source;
  }
}
