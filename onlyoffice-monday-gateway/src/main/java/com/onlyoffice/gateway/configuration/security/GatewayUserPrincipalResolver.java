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

import com.onlyoffice.common.logging.UserPrincipal;
import com.onlyoffice.common.logging.UserPrincipalResolver;
import com.onlyoffice.gateway.security.MondayAuthenticationPrincipal;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Resolver that extracts a UserPrincipal from Gateway-specific authentication objects. This
 * implementation is specifically designed to work with the Monday authentication mechanism and
 * extracts user principals from Authentication objects that contain a
 * MondayAuthenticationPrincipal.
 */
@Component
public class GatewayUserPrincipalResolver implements UserPrincipalResolver {

  /**
   * Resolves a UserPrincipal from the provided Authentication object.
   *
   * @param authentication The Spring Security Authentication object to extract the principal from.
   *     Can be null, in which case an empty Optional is returned.
   * @return An Optional containing the UserPrincipal if the authentication contains a
   *     MondayAuthenticationPrincipal, or an empty Optional otherwise.
   */
  public Optional<UserPrincipal> resolveUserPrincipal(Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() instanceof MondayAuthenticationPrincipal) {
      return Optional.of((MondayAuthenticationPrincipal) authentication.getPrincipal());
    }
    return Optional.empty();
  }
}
