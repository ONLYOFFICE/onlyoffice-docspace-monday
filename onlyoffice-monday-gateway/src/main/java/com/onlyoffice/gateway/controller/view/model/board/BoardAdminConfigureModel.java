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
package com.onlyoffice.gateway.controller.view.model.board;

import com.onlyoffice.gateway.controller.view.model.LoginModel;
import com.onlyoffice.gateway.controller.view.model.settings.SettingsConfigureInformationModel;
import com.onlyoffice.gateway.controller.view.model.settings.SettingsLoginFormModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BoardAdminConfigureModel {
  @Builder.Default
  private SettingsLoginFormModel settingsForm = SettingsLoginFormModel.builder().build();

  @Builder.Default private LoginModel login = LoginModel.builder().build();

  @Builder.Default
  private SettingsConfigureInformationModel information =
      SettingsConfigureInformationModel.builder().build();
}
