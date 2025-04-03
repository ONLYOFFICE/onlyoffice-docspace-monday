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
package com.onlyoffice.gateway.configuration.i18n;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
@RequiredArgsConstructor
public class InternalizationConfiguration implements WebMvcConfigurer {
  @Autowired private final InternalizationLocaleInterceptor localeInterceptor;

  @Bean
  public AcceptHeaderLocaleResolver resolver() {
    var resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(Locale.ENGLISH);
    return resolver;
  }

  @Bean
  @Scope(value = WebApplicationContext.SCOPE_REQUEST)
  public InternalizationLocaleContext localHolder() {
    return new InternalizationLocaleContext();
  }

  @Override
  public void addInterceptors(InterceptorRegistry interceptorRegistry) {
    interceptorRegistry.addInterceptor(localeInterceptor);
  }
}
