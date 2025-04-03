/**
 * (c) Copyright Ascensio System SIA 2025
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onlyoffice.gateway.configuration.security;

import com.onlyoffice.gateway.security.MondayAuthenticationFilter;
import com.onlyoffice.gateway.security.MondayWebhookAuthenticationFilter;
import com.onlyoffice.gateway.security.RateLimiterFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityFilterChainConfiguration {
  @Value("${server.origin}")
  private String selfOrigin;

  private final RateLimiterFilter rateLimiterFilter;
  private final MondayAuthenticationFilter mondayAuthenticationFilter;
  private final MondayWebhookAuthenticationFilter mondayWebhookAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/main.css")
                    .permitAll()
                    .requestMatchers("/webSocket.js")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(rateLimiterFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAt(mondayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(
            mondayWebhookAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .csrf(
            AbstractHttpConfigurer
                ::disable) // No need to use it here, since we rely on Monday's sessionToken
        .cors(c -> c.configurationSource(configurationSource()))
        .build();
  }

  @Bean
  public CorsConfigurationSource configurationSource() {
    var cs = new UrlBasedCorsConfigurationSource();
    var cc = new CorsConfiguration();

    cc.setAllowCredentials(true);
    cc.setAllowedHeaders(List.of("*"));
    cc.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    cc.setAllowedOriginPatterns(List.of("https://*.monday.com", selfOrigin));

    cs.registerCorsConfiguration("/**", cc);
    return cs;
  }
}
