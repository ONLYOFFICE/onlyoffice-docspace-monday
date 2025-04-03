/**
 * (c) Copyright Ascensio System SIA 2024
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.onlyoffice.common.tenant.transfer.request.query.FindEntity;
import com.onlyoffice.tenant.exception.TenantNotFoundException;
import com.onlyoffice.tenant.persistence.entity.Docspace;
import com.onlyoffice.tenant.persistence.entity.Tenant;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
public class BasicTenantQueryServiceTest {
  @Mock private TenantRepository tenantRepository;
  @InjectMocks private BasicTenantQueryService service;

  @BeforeEach
  public void setup() {
    MDC.clear();
  }

  @Nested
  class FindMethodTests {
    @Test
    void shouldReturnTenantCredentials_WhenTenantExists() {
      var query = FindEntity.builder().id(1).build();
      var docspace =
          Docspace.builder()
              .url("https://docspace.example.com")
              .adminLogin("admin@example.com")
              .adminHash("adminHash")
              .build();
      var tenant = Tenant.builder().id(1).docspace(docspace).build();

      when(tenantRepository.findById(query.getId())).thenReturn(Optional.of(tenant));

      var result = service.find(query);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(tenant.getId());
      assertThat(result.getDocSpaceUrl()).isEqualTo(docspace.getUrl());
      assertThat(result.getDocSpaceLogin()).isEqualTo(docspace.getAdminLogin());
      assertThat(result.getDocSpaceHash()).isEqualTo(docspace.getAdminHash());
      verify(tenantRepository).findById(query.getId());
      verifyNoMoreInteractions(tenantRepository);
    }

    @Test
    void shouldThrowTenantNotFoundException_WhenTenantDoesNotExist() {
      var query = FindEntity.builder().id(1).build();

      when(tenantRepository.findById(query.getId())).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.find(query))
          .isInstanceOf(TenantNotFoundException.class)
          .hasMessageContaining("Could not find tenant with id 1");
      verify(tenantRepository).findById(query.getId());
      verifyNoMoreInteractions(tenantRepository);
    }
  }
}
