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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class MondayAuthenticationFilter extends OncePerRequestFilter {
  private static final String SESSION_TOKEN_PARAM = "sessionToken";
  private static final String HX_CURRENT_URL = "HX-Current-URL";
  private final ObjectMapper mapper = new ObjectMapper();

  private final JwtDecoder decoder;
  private final AuthenticationManager authenticationManager;

  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var token = request.getParameter(SESSION_TOKEN_PARAM);
    var tokenHeader =
        getHeaderIgnoreCase(request, HX_CURRENT_URL)
            .or(() -> getHeaderIgnoreCase(request, HttpHeaders.REFERER))
            .orElse(Strings.EMPTY);

    if (token == null || token.isBlank()) {
      var params =
          UriComponentsBuilder.fromUri(URI.create(tokenHeader))
              .build()
              .getQueryParams()
              .get("sessionToken");
      if (params != null && !params.isEmpty()) token = params.getFirst();
    }

    if (token == null || token.isBlank()) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      return;
    }

    try {
      var decodedToken = decoder.decode(token);
      if (decodedToken.getClaim("dat") instanceof Map<?, ?> data) {
        var principal = mapper.convertValue(data, MondayAuthenticationPrincipal.class);
        var authenticated =
            authenticationManager.authenticate(new MondayAuthentication(principal, token));
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        chain.doFilter(request, response);
        return;
      }

      throw new JwtException("Could not decode Monday session token");
    } catch (JwtException e) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
    }
  }

  protected boolean shouldNotFilter(HttpServletRequest request) {
    var path = request.getRequestURI();
    return path.startsWith("/actuator")
        || path.startsWith("/api/1.0/webhook")
        || path.contains("main.css")
        || path.contains("webSocket.js");
  }

  private Optional<String> getHeaderIgnoreCase(HttpServletRequest request, String headerName) {
    return Collections.list(request.getHeaderNames()).stream()
        .filter(name -> name.equalsIgnoreCase(headerName))
        .findFirst()
        .map(request::getHeader);
  }
}
