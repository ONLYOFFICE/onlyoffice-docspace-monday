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
package com.onlyoffice.tenant.security;

import com.onlyoffice.common.logging.UserPrincipal;
import com.onlyoffice.common.logging.UserPrincipalResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;


/**
 * Implementation of {@link UserPrincipalResolver} for the tenant service.
 * <p>
 * This resolver extracts tenant/user information from the authentication context.
 * Since the tenant service may not have direct user authentication, it attempts to
 * derive identity information from authentication details or headers and falls back
 * to system values when necessary.
 * </p>
 *
 * @see UserPrincipal
 * @see UserPrincipalResolver
 */
@Slf4j
@Component
public class TenantUserPrincipalResolver implements UserPrincipalResolver {
  /**
   * Identifier used for system-level operations when no specific user is authenticated.
   */
  private static final long SYSTEM_ID = -1;

  /**
   * Role assigned to system-level operations.
   */
  private static final String SYSTEM_ROLE = "SERVICE";

  /**
   * Resolves user principal information from the provided authentication object.
   * <p>
   * The resolution follows this sequence:
   * <ol>
   *   <li>If authentication is null, returns empty</li>
   *   <li>If authentication principal is already a UserPrincipal, returns it</li>
   *   <li>If authentication details contain a map with user/tenant info, extracts it</li>
   *   <li>Falls back to a system principal with any available tenant ID</li>
   * </ol>
   * </p>
   *
   * @param authentication The authentication object from which to extract user information
   * @return An Optional containing the resolved UserPrincipal if successful, empty otherwise
   */
  public Optional<UserPrincipal> resolveUserPrincipal(Authentication authentication) {
    if (authentication == null)
      return Optional.empty();

    if (authentication.getPrincipal() instanceof UserPrincipal principal)
      return Optional.of(principal);

    if (authentication.getDetails() instanceof Map<?, ?> details) {
      try {
        var extractedPrincipal = extractFromDetailsMap(details);
        if (extractedPrincipal.isPresent())
          return extractedPrincipal;
      } catch (Exception e) {
        log.debug("Could not extract user principal from authentication details", e);
      }
    }

    return Optional.of(
            TenantUserPrincipal.builder()
                    .userId(SYSTEM_ID)
                    .accountId(extractTenantId(authentication))
                    .role(SYSTEM_ROLE)
                    .build());
  }

  /**
   * Attempts to extract a UserPrincipal from the authentication details map.
   *
   * @param details Map containing authentication details
   * @return Optional containing UserPrincipal if extraction successful, empty otherwise
   */
  private Optional<UserPrincipal> extractFromDetailsMap(Map<?, ?> details) {
    var tenantId = getDetailAsLong(details, "tenantId", "tenant_id", "accountId", "account_id");
    if (tenantId == null)
      return Optional.empty();

    var userId = getDetailAsLong(details, "userId", "user_id");
    var role = getDetailAsString(details, "role", "userRole", "user_role");

    return Optional.of(
            TenantUserPrincipal.builder()
                    .userId(userId != null ? userId : SYSTEM_ID)
                    .accountId(tenantId)
                    .role(role)
                    .build());
  }

  /**
   * Extracts a Long value from a details map, trying multiple possible key names.
   *
   * @param details Map containing authentication details
   * @param keys Sequence of possible key names to try
   * @return Extracted Long value or null if not found
   */
  private Long getDetailAsLong(Map<?, ?> details, String... keys) {
    for (var key : keys) {
      var value = details.get(key);
      if (value instanceof Number number) {
        return number.longValue();
      } else if (value instanceof String stringValue) {
        try {
          return Long.parseLong(stringValue);
        } catch (NumberFormatException ignored) {
          // Simply skip
        }
      }
    }

    return null;
  }

  /**
   * Extracts a String value from a details map, trying multiple possible key names.
   *
   * @param details Map containing authentication details
   * @param keys Sequence of possible key names to try
   * @return Extracted String value or null if not found
   */
  private String getDetailAsString(Map<?, ?> details, String... keys) {
    for (var key : keys) {
      var value = details.get(key);
      if (value instanceof String stringValue)
        return stringValue;
    }

    return null;
  }

  /**
   * Extracts the tenant ID from the authentication object.
   *
   * @param authentication The authentication object
   * @return The tenant ID if found, or SYSTEM_ID as fallback
   */
  private long extractTenantId(Authentication authentication) {
    if (authentication.getDetails() instanceof Map<?, ?> details) {
      var tenantId = getDetailAsLong(details, "tenantId", "tenant_id", "accountId", "account_id");
      if (tenantId != null)
        return tenantId;
    }

    return SYSTEM_ID;
  }
}
