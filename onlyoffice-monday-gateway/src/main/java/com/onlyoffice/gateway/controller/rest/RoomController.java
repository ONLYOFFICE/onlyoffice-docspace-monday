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
package com.onlyoffice.gateway.controller.rest;

import com.onlyoffice.common.client.notification.factory.NotificationPublisherFactory;
import com.onlyoffice.common.client.notification.transfer.event.NotificationEvent;
import com.onlyoffice.common.client.notification.transfer.event.RoomCreated;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterRoom;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveRoom;
import com.onlyoffice.gateway.client.TenantServiceClient;
import com.onlyoffice.gateway.security.MondayAuthenticationPrincipal;
import com.onlyoffice.gateway.transport.rest.request.CreateRoomCommand;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/1.0/rooms")
public class RoomController {
  private final TenantServiceClient tenantService;
  private final Consumer<NotificationEvent> messagePublisher;

  public RoomController(TenantServiceClient tenantService, NotificationPublisherFactory factory) {
    this.tenantService = tenantService;
    this.messagePublisher = factory.getPublisher("notifications");
  }

  @Secured("ROLE_ADMIN")
  @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
  public ResponseEntity<?> createRoom(
      @AuthenticationPrincipal MondayAuthenticationPrincipal user,
      @ModelAttribute CreateRoomCommand body) {
    try {
      MDC.put("tenant_id", String.valueOf(user.getAccountId()));
      MDC.put("board_id", String.valueOf(body.getBoardId()));
      MDC.put("user_id", String.valueOf(user.getUserId()));
      if (!tenantService.findTenant(user.getAccountId()).getStatusCode().is2xxSuccessful()) {
        log.warn("Could not find tenant");
        return ResponseEntity.badRequest().build();
      }

      var response =
          tenantService.createRoom(
              RegisterRoom.builder()
                  .boardId(body.getBoardId())
                  .tenantId(user.getAccountId())
                  .roomId(body.getRoomId())
                  .mondayUsers(body.getUsers())
                  .build());

      log.info("Board room has been registered");

      messagePublisher.accept(
          RoomCreated.builder().tenantId(user.getAccountId()).boardId(body.getBoardId()).build());

      log.debug("Room created notification has been sent");

      return ResponseEntity.status(response.getStatusCode().value())
          .header("HX-Refresh", "true")
          .build();
    } finally {
      MDC.clear();
    }
  }

  @Secured("ROLE_ADMIN")
  @DeleteMapping("/{boardId}")
  public ResponseEntity<?> unlinkRoom(
      @AuthenticationPrincipal MondayAuthenticationPrincipal user, @PathVariable long boardId) {
    try {
      var response =
          tenantService.removeRoom(
              RemoveRoom.builder().tenantId(user.getAccountId()).boardId(boardId).build());
      return ResponseEntity.status(response.getStatusCode().value())
          .header("HX-Refresh", "true")
          .build();
    } finally {
      MDC.clear();
    }
  }
}
