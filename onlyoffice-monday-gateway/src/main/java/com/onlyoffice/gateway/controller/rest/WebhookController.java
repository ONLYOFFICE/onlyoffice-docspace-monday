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

import com.onlyoffice.common.tenant.transfer.request.command.RemoveTenant;
import com.onlyoffice.gateway.client.TenantServiceClient;
import com.onlyoffice.gateway.transport.rest.request.WebhookCommandTenantPayload;
import com.onlyoffice.gateway.transport.rest.request.WebhookWrapperCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/1.0/webhook")
public class WebhookController {
  private final TenantServiceClient tenantClient;

  @PostMapping
  @Secured("ROLE_ADMIN")
  public ResponseEntity<?> process(
      @RequestBody WebhookWrapperCommand<WebhookCommandTenantPayload> command) {
    if (command.getType().equalsIgnoreCase("uninstall"))
      tenantClient.removeTenant(
          RemoveTenant.builder().tenantId(command.getData().getTenantId()).build());

    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
