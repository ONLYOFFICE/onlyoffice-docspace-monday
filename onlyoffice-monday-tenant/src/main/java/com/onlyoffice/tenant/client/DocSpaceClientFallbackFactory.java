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
package com.onlyoffice.tenant.client;

import com.onlyoffice.common.docspace.transfer.request.command.AuthenticateUser;
import com.onlyoffice.common.docspace.transfer.request.command.ChangeRoomAccess;
import com.onlyoffice.common.docspace.transfer.response.GenericResponse;
import com.onlyoffice.common.docspace.transfer.response.MembersAccess;
import com.onlyoffice.common.docspace.transfer.response.RoomLink;
import com.onlyoffice.common.docspace.transfer.response.UserToken;
import com.onlyoffice.tenant.exception.DocSpaceServiceException;
import java.net.URI;
import java.util.List;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class DocSpaceClientFallbackFactory implements FallbackFactory<DocSpaceClient> {
  public DocSpaceClient create(Throwable cause) {
    return new DocSpaceClient() {
      public GenericResponse<UserToken> generateToken(URI baseUri, AuthenticateUser command) {
        throw new DocSpaceServiceException("Could not generate authentication token", cause);
      }

      public GenericResponse<List<RoomLink>> generateSharedKey(
          URI baseUri, long roomId, String token) {
        throw new DocSpaceServiceException(
            "Could not generate shared key for room " + roomId, cause);
      }

      public GenericResponse<MembersAccess> changeRoomAccess(
          URI baseUri, long roomId, String token, ChangeRoomAccess command) {
        throw new DocSpaceServiceException(
            "Could not change room access for room " + roomId, cause);
      }
    };
  }
}
