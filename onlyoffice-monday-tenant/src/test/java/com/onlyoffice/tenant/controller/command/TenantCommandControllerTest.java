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
package com.onlyoffice.tenant.controller.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterTenant;
import com.onlyoffice.common.tenant.transfer.response.TenantCredentials;
import com.onlyoffice.tenant.controller.GlobalControllerAdvice;
import com.onlyoffice.tenant.service.command.TenantCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class TenantCommandControllerTest {
  private MockMvc mvc;
  @Mock private TenantCommandService commandService;
  @InjectMocks private TenantCommandController controller;

  private JacksonTester<RegisterTenant> jsonRegisterTenant;

  @BeforeEach
  public void setup() {
    JacksonTester.initFields(
        this,
        JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build());
    mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalControllerAdvice())
            .build();
  }

  @Test
  void shouldRegisterTenant_WhenValidRequest_ThenReturnCreatedStatus() throws Exception {
    var command =
        RegisterTenant.builder()
            .id(1)
            .url("https://mock.com")
            .adminLogin("admin@admin.com")
            .adminHash("hash")
            .docSpaceUserId("aaa-aaa-aaa")
            .mondayUserId(1)
            .build();

    var tenantCredentials =
        TenantCredentials.builder()
            .id(1)
            .docSpaceUrl("https://docspace.com")
            .docSpaceLogin("admin@admin.com")
            .docSpaceHash("hash")
            .build();

    Mockito.when(commandService.register(Mockito.any(RegisterTenant.class)))
        .thenReturn(tenantCredentials);

    var response =
        mvc.perform(
                post("/tenants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRegisterTenant.write(command).getJson()))
            .andReturn()
            .getResponse();

    Mockito.verify(commandService)
        .register(Mockito.argThat(c -> c.getAdminLogin().equals(command.getAdminLogin())));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
  }

  @Test
  void shouldNotRegisterTenant_WhenInvalidRequest_ThenReturnBadRequest() throws Exception {
    var command = RegisterTenant.builder().build();

    var response =
        mvc.perform(
                post("/tenants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRegisterTenant.write(command).getJson()))
            .andReturn()
            .getResponse();

    Mockito.verifyNoInteractions(commandService);
    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
