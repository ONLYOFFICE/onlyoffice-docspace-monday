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
package com.onlyoffice.tenant.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterTenant;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveTenant;
import com.onlyoffice.common.tenant.transfer.response.TenantCredentials;
import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.common.user.transfer.request.command.RemoveTenantUsers;
import com.onlyoffice.tenant.exception.OutboxSerializationException;
import com.onlyoffice.tenant.persistence.entity.Docspace;
import com.onlyoffice.tenant.persistence.entity.Outbox;
import com.onlyoffice.tenant.persistence.entity.OutboxType;
import com.onlyoffice.tenant.persistence.repository.OutboxRepository;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BasicTenantCommandService implements TenantCommandService {
  private final ObjectMapper objectMapper;
  private final OutboxRepository outboxRepository;
  private final TenantRepository tenantRepository;

  @CacheEvict(value = "tenants", key = "#command.id")
  @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
  public TenantCredentials register(@Valid RegisterTenant command) {
    try {
      MDC.put("tenant_id", String.valueOf(command.getId()));
      MDC.put("monday_user_id", String.valueOf(command.getMondayUserId()));
      MDC.put("docSpace_user_id", command.getDocSpaceUserId());
      MDC.put("docSpace_url", command.getUrl());
      log.info("Registering a new user entry");

      var now = System.currentTimeMillis();
      var docspace =
          Docspace.builder()
              .url(command.getUrl())
              .adminLogin(command.getAdminLogin())
              .adminHash(command.getAdminHash())
              .build();
      var tenant =
          com.onlyoffice.tenant.persistence.entity.Tenant.builder()
              .id(command.getId())
              .docspace(docspace)
              .build();

      docspace.setTenant(tenant);
      tenantRepository.save(tenant);

      outboxRepository.save(
          Outbox.builder()
              .type(OutboxType.CREATE_USER_ON_INITIALIZATION)
              .payload(
                  objectMapper.writeValueAsString(
                      CommandMessage.<RegisterUser>builder()
                          .commandAt(now)
                          .payload(
                              RegisterUser.builder()
                                  .tenantId(command.getId())
                                  .mondayId(command.getMondayUserId())
                                  .docSpaceId(command.getDocSpaceUserId())
                                  .email(command.getAdminLogin())
                                  .hash(command.getAdminHash())
                                  .build())
                          .build()))
              .build());

      return TenantCredentials.builder().id(tenant.getId()).build();
    } catch (JsonProcessingException e) {
      log.error("Could not perform json serialization", e);
      throw new OutboxSerializationException(e);
    } finally {
      MDC.clear();
    }
  }

  @CacheEvict(value = "tenants", key = "#command.tenantId")
  @Transactional(timeout = 2, rollbackFor = Exception.class)
  public boolean remove(RemoveTenant command) {
    var tenant = tenantRepository.getReferenceById(command.getTenantId());
    try {
      MDC.put("tenant_id", String.valueOf(command.getTenantId()));
      tenantRepository.delete(tenant);
      outboxRepository.save(
          Outbox.builder()
              .type(OutboxType.REMOVE_TENANT_USERS)
              .payload(
                  objectMapper.writeValueAsString(
                      CommandMessage.<RemoveTenantUsers>builder()
                          .commandAt(System.currentTimeMillis())
                          .payload(
                              RemoveTenantUsers.builder().tenantId(command.getTenantId()).build())
                          .build()))
              .build());
      return true;
    } catch (JsonProcessingException e) {
      log.warn("Could not perform json serialization", e);
      return false;
    } catch (Exception e) {
      log.warn("Could not remove current tenant", e);
      return false;
    } finally {
      MDC.clear();
    }
  }
}
