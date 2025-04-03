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

import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class MondayAuthentication implements Authentication {
  private final MondayAuthenticationPrincipal principal;
  private final String signature;
  private boolean isAuthenticated;

  private String getRole() {
    if (principal.isViewOnly()) {
      return MondayRoles.VIEWER.getRole();
    } else if (principal.isAdmin()) {
      return MondayRoles.ADMIN.getRole();
    } else if (principal.isGuest()) {
      return MondayRoles.GUEST.getRole();
    } else {
      return MondayRoles.ROLE_MEMBER.getRole();
    }
  }

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(getRole()));
  }

  public Object getCredentials() {
    return signature;
  }

  public Object getDetails() {
    return getRole();
  }

  public Object getPrincipal() {
    return principal;
  }

  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    this.isAuthenticated = isAuthenticated;
  }

  public String getName() {
    return String.valueOf(principal.getUserId());
  }
}
