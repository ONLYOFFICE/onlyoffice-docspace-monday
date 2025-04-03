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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

@Component
@RequiredArgsConstructor
public class InternalizationLocaleInterceptor implements HandlerInterceptor {
  private final MessageSourceLocaleService localeService;

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws ServletException {
    var localeResolver = RequestContextUtils.getLocaleResolver(request);
    if (localeResolver == null)
      throw new IllegalStateException("Could not find any locale resolver");

    if (localeResolver instanceof AcceptHeaderLocaleResolver resolver)
      localeService.localeContext().setCurrent(resolver.resolveLocale(request));
    else
      throw new IllegalStateException(
          "Registered resolver must be of type AcceptHeaderLocaleResolver");

    return true;
  }
}
