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
public class SettingsModalModel {
  @Builder.Default private String confirmActionText = "Confirm your action";

  @Builder.Default
  private String agreementText = "Do you agree you want to change DocSpace portal address?";

  @Builder.Default private String changeText = "Change";
  @Builder.Default private String cancelText = "Cancel";

  @Builder.Default
  private String changeError = "Could not remove current DocSpace data. Please try again later";
}
