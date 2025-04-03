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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SettingsLoginFormModel {
  @Builder.Default private String loginText = "Login";
  @Builder.Default private String changeText = "Change DocSpace";

  @Builder.Default
  private String disclaimer =
      "By clicking this button, you agree that rooms will be created in DocSpace on your behalf, room tags will be created, users will be invited to rooms and users will be created in DocSpace";

  @Builder.Default
  private SettingsLoginFormFields fields = SettingsLoginFormFields.builder().build();

  @Getter
  @Setter
  @Builder
  public static class SettingsLoginFormFields {
    @Builder.Default private String docSpaceLabel = "DocSpace Server Address";
    @Builder.Default private String docSpacePlaceholder = "Please enter DocSpace address";
    @Builder.Default private String emailLabel = "DocSpace Email";
    @Builder.Default private String emailPlaceholder = "Please enter DocSpace email";
    @Builder.Default private String passwordLabel = "DocSpace Password";
    @Builder.Default private String passwordPlaceholder = "Please enter DocSpace password";
  }
}
