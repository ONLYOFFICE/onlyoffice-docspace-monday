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
package com.onlyoffice.gateway.controller.view.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginModel {
  @Builder.Default private String url = "";
  @Builder.Default private String email = "";
  @Builder.Default private String hash = "";
  @Builder.Default private String accessText = "Monday requests access to your ONLYOFFICE DocSpace";
  @Builder.Default private String addressText = "docspace.server.info";
  @Builder.Default private String success = "Login successful";
  @Builder.Default private String error = "Login failed";

  @Builder.Default
  private String cspError = "Error during initialization. Please check your DocSpace CSP settings";

  @Builder.Default
  private String sizeHeaderError = "Your window is too small to display all page contents";

  @Builder.Default
  private String sizeHeaderText = "Please resize the window or enable full-screen mode";
}
