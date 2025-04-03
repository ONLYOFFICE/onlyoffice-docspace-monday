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
package com.onlyoffice.tenant.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterTenant;
import com.onlyoffice.tenant.exception.OutboxSerializationException;
import com.onlyoffice.tenant.persistence.entity.Outbox;
import com.onlyoffice.tenant.persistence.entity.OutboxType;
import com.onlyoffice.tenant.persistence.entity.Tenant;
import com.onlyoffice.tenant.persistence.repository.OutboxRepository;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
public class BasicTenantCommandServiceTest {
  @Mock private ObjectMapper objectMapper;
  @Mock private OutboxRepository outboxRepository;
  @Mock private TenantRepository tenantRepository;
  @InjectMocks private BasicTenantCommandService service;

  @BeforeEach
  public void setup() {
    MDC.clear();
  }

  @Nested
  class RegisterMethodTests {
    @Test
    void shouldRegister_WhenSuccessful_ThenSaveTenantAndOutboxEntries() throws Exception {
      var command =
          RegisterTenant.builder()
              .id(1)
              .mondayUserId(2)
              .docSpaceUserId("docSpaceUser1")
              .url("https://docspace.example.com")
              .adminLogin("admin@example.com")
              .adminHash("adminHash")
              .build();

      when(objectMapper.writeValueAsString(any())).thenReturn("mock-payload");

      var credentials = service.register(command);

      verify(tenantRepository)
          .save(
              argThat(
                  tenant ->
                      tenant.getId() == command.getId()
                          && tenant.getDocspace() != null
                          && tenant.getDocspace().getUrl().equals(command.getUrl())
                          && tenant.getDocspace().getAdminLogin().equals(command.getAdminLogin())
                          && tenant.getDocspace().getAdminHash().equals(command.getAdminHash())));
      verify(outboxRepository, times(1)).save(any(Outbox.class));
      verify(outboxRepository)
          .save(argThat(outbox -> outbox.getType() == OutboxType.CREATE_USER_ON_INITIALIZATION));
      assertThat(credentials).isNotNull();
      assertThat(credentials.getId()).isEqualTo(command.getId());
    }

    @Test
    void shouldRegister_WhenSerializationFails_ThrowOutboxSerializationException()
        throws Exception {
      var command =
          RegisterTenant.builder()
              .id(1)
              .mondayUserId(2)
              .docSpaceUserId("docSpaceUser1")
              .url("https://docspace.example.com")
              .adminLogin("admin@example.com")
              .adminHash("adminHash")
              .build();

      when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
      assertThatThrownBy(() -> service.register(command))
          .isInstanceOf(OutboxSerializationException.class);
      verify(tenantRepository).save(any(Tenant.class));
      verify(outboxRepository, never()).save(any(Outbox.class));
    }
  }
}
