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
import lombok.Builder;
import lombok.Getter;

/**
 * Implementation of the UserPrincipal interface for the tenant service. Since tenant service might
 * not have an actual user authentication system, this is a simple implementation that can be used
 * for service-to-service communication.
 */
@Getter
@Builder
public class TenantUserPrincipal implements UserPrincipal {
  private long userId;
  private long accountId;
  private String role;
}
