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
package com.onlyoffice.tenant.service.query;

import com.onlyoffice.common.tenant.transfer.request.query.FindEntity;
import com.onlyoffice.common.tenant.transfer.response.TenantCredentials;
import com.onlyoffice.tenant.exception.TenantNotFoundException;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BasicTenantQueryService implements TenantQueryService {
  private final TenantRepository tenantRepository;

  @Cacheable(value = "tenants", key = "#query.id")
  public TenantCredentials find(@Valid FindEntity query) {
    try {
      MDC.put("tenant_id", String.valueOf(query.getId()));
      log.info("Trying to find tenant credentials by tenant id");

      var tenant =
          tenantRepository
              .findById(query.getId())
              .orElseThrow(
                  () ->
                      new TenantNotFoundException(
                          String.format("Could not find tenant with id %d", query.getId())));

      return TenantCredentials.builder()
          .id(tenant.getId())
          .docSpaceUrl(tenant.getDocspace().getUrl())
          .docSpaceLogin(tenant.getDocspace().getAdminLogin())
          .docSpaceHash(tenant.getDocspace().getAdminHash())
          .build();
    } finally {
      MDC.clear();
    }
  }
}
