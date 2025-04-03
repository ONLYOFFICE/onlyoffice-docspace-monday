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
package com.onlyoffice.gateway.security;

import com.onlyoffice.gateway.configuration.security.DistributedRateLimiterFactoryConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {
  private final String X_FORWARDED_FOR = "X-Forwarded-For";

  private final DistributedRateLimiterFactoryConfiguration bucketFactory;
  private final LettuceBasedProxyManager proxyManager;

  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      var method = request.getMethod();
      var bucketConfigurationFactory =
          bucketFactory.bucketConfigurationFactory().apply(HttpMethod.valueOf(method));
      var client = request.getHeader(X_FORWARDED_FOR);
      if (client == null || client.isBlank()) client = request.getRemoteAddr();
      var bucket =
          proxyManager
              .builder()
              .build(
                  String.format("%s:%s", method, client).getBytes(StandardCharsets.UTF_8),
                  bucketConfigurationFactory);
      var probe = bucket.tryConsumeAndReturnRemaining(1);
      if (!probe.isConsumed()) {
        MDC.put("client", client);
        log.debug("Client has exhausted all tokens");
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        return;
      }

      chain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
