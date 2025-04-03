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

import com.onlyoffice.common.tenant.transfer.request.command.RegisterDocSpace;
import com.onlyoffice.tenant.exception.TenantNotFoundException;
import com.onlyoffice.tenant.persistence.entity.Docspace;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import jakarta.validation.Valid;
import java.util.Set;
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
public class BasicDocSpaceCommandService implements DocSpaceCommandService {
  private final TenantRepository tenantRepository;

  @CacheEvict(value = "tenants", key = "#command.tenantId")
  @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public void register(@Valid RegisterDocSpace command) {
    try {
      MDC.put("tenant_id", String.valueOf(command.getTenantId()));
      MDC.put("docspace_url", command.getUrl());
      log.info("Registering new DocSpace credentials for this tenant");

      var tenant =
          tenantRepository
              .findById(command.getTenantId())
              .orElseThrow(
                  () ->
                      new TenantNotFoundException(
                          String.format(
                              "Could not find tenant with id %d", command.getTenantId())));

      if (!command.getUrl().equals(tenant.getDocspace().getUrl())) {
        tenant.setBoards(Set.of());
        tenant.setDocspace(
            Docspace.builder()
                .url(command.getUrl())
                .adminLogin(command.getAdminLogin())
                .adminHash(command.getAdminHash())
                .build());
        return;
      }

      var docspace = tenant.getDocspace();
      docspace.setAdminLogin(command.getAdminLogin());
      docspace.setAdminHash(command.getAdminHash());
    } finally {
      MDC.clear();
    }
  }
}
