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
package com.onlyoffice.gateway.controller.view;

import lombok.Getter;

@Getter
public enum TemplateLocation {
  NOT_CONFIGURED_ERROR("pages/errors/configuration"),
  NO_ROOM_ERROR("pages/errors/room"),
  SERVER_ERROR("pages/errors/server"),
  BOARD_ADMIN_CONFIGURE("pages/board/configure"),
  ADMIN_CONFIGURE("pages/settings/configure/admin"),
  ADMIN_LOGIN("pages/settings/login/admin"),
  USER_LOGIN("pages/settings/login/user");
  private final String path;

  TemplateLocation(String path) {
    this.path = path;
  }
}
