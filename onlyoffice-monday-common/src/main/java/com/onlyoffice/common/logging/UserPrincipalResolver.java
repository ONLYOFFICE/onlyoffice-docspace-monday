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
package com.onlyoffice.common.logging;

import java.util.Optional;
import org.springframework.security.core.Authentication;

/**
 * Interface for resolving user principals from various authentication objects. Each service should
 * implement this interface to provide custom user principal resolution.
 */
public interface UserPrincipalResolver {
  /**
   * Resolves a UserPrincipal from an Authentication object.
   *
   * @param authentication The Spring Security Authentication object
   * @return An Optional containing the UserPrincipal if it could be resolved, or empty if not
   */
  Optional<UserPrincipal> resolveUserPrincipal(Authentication authentication);
}
