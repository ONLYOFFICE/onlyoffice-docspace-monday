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
package com.onlyoffice.tenant.controller.command;

import com.onlyoffice.common.tenant.transfer.request.command.RegisterRoom;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveRoom;
import com.onlyoffice.tenant.client.UserServiceClient;
import com.onlyoffice.tenant.service.command.BoardCommandService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// TODO: Add Handlers with X-Timeout
@Validated
@RestController
@RequestMapping(
    value = "/tenants/boards",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class BoardCommandController {
  private final UserServiceClient userService;
  private final BoardCommandService commandService;

  @PostMapping("/room")
  @RateLimiter(name = "registerRoom")
  public ResponseEntity<?> registerRoom(@RequestBody @Valid RegisterRoom command) {
    var users = userService.findDocSpaceUsers(command.getTenantId(), command.getMondayUsers());
    if (!users.getStatusCode().is2xxSuccessful() || users.getBody() == null)
      return ResponseEntity.badRequest().build();

    commandService.register(command, users.getBody().getIds());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/room")
  @RateLimiter(name = "removeRoom")
  public ResponseEntity<?> deleteRoom(@RequestBody @Valid RemoveRoom command) {
    commandService.remove(command);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
