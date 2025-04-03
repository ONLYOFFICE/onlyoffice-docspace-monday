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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MondayAuthenticationManager implements AuthenticationManager {
  private final List<AuthenticationProvider> providers;

  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    var aclass = authentication.getClass();
    for (var provider : providers) {
      if (provider.supports(aclass)) {
        return provider.authenticate(authentication);
      }
    }

    throw new ProviderNotFoundException(
        String.format(
            "Could not find any appropriate AuthenticationProvider for %s type",
            authentication.getClass().getName()));
  }
}
