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

import com.onlyoffice.common.tenant.transfer.request.command.*;
import com.onlyoffice.common.tenant.transfer.response.BoardInformation;
import com.onlyoffice.common.tenant.transfer.response.TenantCredentials;
import com.onlyoffice.gateway.exception.service.ServiceBadRequestException;
import java.util.concurrent.ExecutionException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TenantServiceClientFallbackFactory implements FallbackFactory<TenantServiceClient> {
  public TenantServiceClient create(Throwable cause) {
    return new TenantServiceClient() {
      public ResponseEntity<TenantCredentials> createTenant(RegisterTenant command) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<TenantCredentials> findTenant(long tenantId) {
        if (cause instanceof ExecutionException e
            && e.getCause() instanceof ServiceBadRequestException)
          return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      public ResponseEntity<BoardInformation> findBoard(long boardId) {
        if (cause instanceof ExecutionException e
            && e.getCause() instanceof ServiceBadRequestException)
          return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      public ResponseEntity<?> createRoom(RegisterRoom command) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<?> removeRoom(RemoveRoom command) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<?> removeTenant(RemoveTenant command) {
        return ResponseEntity.badRequest().build();
      }

      public ResponseEntity<?> updateDocSpace(RegisterDocSpace command) {
        return ResponseEntity.badRequest().build();
      }
    };
  }
}
