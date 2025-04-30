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

/**
 * Interface representing a user principal with common properties needed for logging. Services
 * should implement or adapt to this interface to enable consistent logging.
 */
public interface UserPrincipal {
  /**
   * Gets the ID of the user.
   *
   * @return The user ID
   */
  long getUserId();

  /**
   * Gets the account/tenant ID the user belongs to.
   *
   * @return The account/tenant ID
   */
  long getAccountId();

  /**
   * Gets the user's role.
   *
   * @return The user's role or null if not available
   */
  String getRole();
}
