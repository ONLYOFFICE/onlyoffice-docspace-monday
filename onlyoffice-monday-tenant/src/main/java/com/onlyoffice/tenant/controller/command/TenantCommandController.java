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

import com.fasterxml.jackson.annotation.JsonView;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterTenant;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveTenant;
import com.onlyoffice.common.tenant.transfer.response.TenantCredentials;
import com.onlyoffice.common.tenant.transfer.response.View;
import com.onlyoffice.tenant.service.command.TenantCommandService;
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
    value = "/tenants",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class TenantCommandController {
  private final TenantCommandService commandService;

  @PostMapping
  @RateLimiter(name = "registerTenant")
  @JsonView(View.RegisterTenantView.class)
  public ResponseEntity<TenantCredentials> registerTenant(
      @RequestBody @Valid RegisterTenant command) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commandService.register(command));
  }

  @DeleteMapping
  @RateLimiter(name = "registerTenant")
  public ResponseEntity<?> removeTenant(@RequestBody @Valid RemoveTenant command) {
    return ResponseEntity.status(
            commandService.remove(command) ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
        .build();
  }
}
