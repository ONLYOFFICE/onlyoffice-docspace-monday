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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterDocSpace;
import com.onlyoffice.tenant.controller.GlobalControllerAdvice;
import com.onlyoffice.tenant.service.command.DocSpaceCommandService;
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
public class DocspaceCommandControllerTest {
  private MockMvc mvc;
  @Mock private DocSpaceCommandService commandService;
  @InjectMocks private DocspaceCommandController controller;

  private JacksonTester<RegisterDocSpace> jsonRegisterDocSpace;

  @BeforeEach
  public void setup() {
    JacksonTester.initFields(this, new ObjectMapper());
    mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalControllerAdvice())
            .build();
  }

  @Test
  void shouldRegisterDocSpace_WhenValidRequest_ThenReturnOkStatus() throws Exception {
    var command =
        RegisterDocSpace.builder()
            .tenantId(1)
            .url("https://docspace.example.com")
            .adminLogin("admin@example.com")
            .adminHash("encryptedAdminHash")
            .build();

    var response =
        mvc.perform(
                post("/tenants/docspace")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRegisterDocSpace.write(command).getJson()))
            .andReturn()
            .getResponse();

    Mockito.verify(commandService)
        .register(
            Mockito.argThat(
                c ->
                    c.getTenantId() == command.getTenantId()
                        && c.getUrl().equals(command.getUrl())
                        && c.getAdminLogin().equals(command.getAdminLogin())
                        && c.getAdminHash().equals(command.getAdminHash())));
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void shouldNotRegisterDocSpace_WhenInvalidRequest_ThenReturnBadRequest() throws Exception {
    var command =
        RegisterDocSpace.builder()
            .url("https://docspace.example.com")
            .adminLogin("admin@example.com")
            .adminHash("encryptedAdminHash")
            .build();

    var response =
        mvc.perform(
                post("/tenants/docspace")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRegisterDocSpace.write(command).getJson()))
            .andReturn()
            .getResponse();

    Mockito.verifyNoInteractions(commandService);
    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
