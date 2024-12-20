/**
 *
 * (c) Copyright Ascensio System SIA 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.onlyoffice.gateway.controller.view.model.board;

import com.onlyoffice.gateway.controller.view.model.LoginModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DocSpaceBoardModel {
  @Builder.Default private LoginModel login = LoginModel.builder().build();

  @Builder.Default
  private DocSpaceBoardManagerModel docSpaceManager = DocSpaceBoardManagerModel.builder().build();

  @Getter
  @Setter
  @Builder
  public static class DocSpaceBoardManagerModel {
    @Builder.Default private String accessKey = "";
    @Builder.Default private long roomId = -1;

    @Builder.Default
    private String notificationText = "Public room is now available for this board";

    @Builder.Default private String welcomeText = "Welcome to DocSpace";

    @Builder.Default
    private String notPublicText =
        "Board room is not public yet. Please login or wait for it to become public";

    @Builder.Default private String unlinkText = "Unlink";
    @Builder.Default private String unlinkRoomHeader = "Oops!";

    @Builder.Default
    private String unlinkRoomText =
        "Apparently the room linked to your board is archived/removed. Do you want to unlink it?";
  }
}
