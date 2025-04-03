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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BoardCreateRoomModel {
  private LoginModel login;
  private BoardCreateRoomInformationModel creationInformation;
  @Builder.Default private String roomsQuotaError = "You are out of rooms quota";
  @Builder.Default private String timeoutError = "Operation timed out";
  @Builder.Default private String operationError = "Could not perform current operation";

  @Getter
  @Setter
  @Builder
  public static class BoardCreateRoomInformationModel {
    @Builder.Default private String welcomeText = "Welcome to DocSpace Board!";
    @Builder.Default private String createText = "Please create the room";
    @Builder.Default private String buttonText = "Create room";

    @Builder.Default
    private String noPermissionsText = "Sorry, you don't have enough permissions to create a room";
  }
}
