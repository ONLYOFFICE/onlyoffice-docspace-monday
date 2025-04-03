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
package com.onlyoffice.gateway.controller.view.model.settings;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsConfigureInformationModel {
  @Builder.Default private String csp = "Check the CSP settings";

  @Builder.Default private String credentialsFirst = "Before connecting the app, please go to the";

  @Builder.Default
  private String credentialsPath = "DocSpace Settings - Developer tools - JavaScript SDK";

  @Builder.Default
  private String credentialsSecond = "and add the following credentials to the allow list:";

  @Builder.Default private String monday = "Monday portal address:";
  private String mondayAddress;
  @Builder.Default private String app = "ONLYOFFICE DocSpace app:";
  private String appAddress;
}
