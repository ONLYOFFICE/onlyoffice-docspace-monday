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
package com.onlyoffice.tenant.service.remote;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.tenant.transfer.event.AccessKeyRefreshed;
import com.onlyoffice.common.tenant.transfer.request.command.InviteRoomUsers;
import com.onlyoffice.common.tenant.transfer.request.command.RefreshAccessKey;
import jakarta.validation.Valid;

public interface DocSpaceRemoteRoomService {
  AccessKeyRefreshed refreshAccessKey(@Valid CommandMessage<RefreshAccessKey> command);

  void inviteUsers(@Valid CommandMessage<InviteRoomUsers> command);
}
