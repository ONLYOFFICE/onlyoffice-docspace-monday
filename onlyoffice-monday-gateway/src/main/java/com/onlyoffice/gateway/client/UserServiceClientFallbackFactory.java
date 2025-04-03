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
package com.onlyoffice.gateway.client;

import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.common.user.transfer.response.DocSpaceUsers;
import com.onlyoffice.common.user.transfer.response.UserCredentials;
import java.util.Set;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
  public UserServiceClient create(Throwable cause) {
    return new UserServiceClient() {
      public ResponseEntity<UserCredentials> findUser(long tenantId, long mondayId) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<UserCredentials> findUser(long tenantId, long mondayId, int timeout) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<DocSpaceUsers> findDocSpaceUsers(long tenantId, Set<Long> ids) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<DocSpaceUsers> findDocSpaceUsers(
          long tenantId, Set<Long> ids, int timeout) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<?> registerUser(RegisterUser command) {
        return ResponseEntity.badRequest().build();
      }
    };
  }
}
