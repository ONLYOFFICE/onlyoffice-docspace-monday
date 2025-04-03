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
package com.onlyoffice.user.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.user.service.command.UserCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class UserCommandControllerTest {
  private MockMvc mvc;
  @Mock private UserCommandService userCommandService;
  @InjectMocks private UserCommandController commandController;
  private JacksonTester<RegisterUser> jsonUser;

  @BeforeEach
  public void setup() {
    JacksonTester.initFields(this, new ObjectMapper());
    mvc =
        MockMvcBuilders.standaloneSetup(commandController)
            .setControllerAdvice(new GlobalControllerAdvice())
            .build();
  }

  @Test
  void shouldRegisterUser_WhenValidRequest_ThenReturnCreatedStatus() throws Exception {
    var registerUser =
        RegisterUser.builder()
            .tenantId(1)
            .mondayId(1)
            .docSpaceId("aaa-aaa-aaa")
            .email("test@example.com")
            .hash("someHash")
            .build();

    var response =
        mvc.perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonUser.write(registerUser).getJson()))
            .andReturn()
            .getResponse();

    Mockito.verify(userCommandService, Mockito.times(1)).register(any(RegisterUser.class));
    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
  }

  @Test
  void shouldNotRegisterUser_WhenInvalidRequest_ThenThrowException() throws Exception {
    var registerUser =
        RegisterUser.builder()
            .tenantId(1)
            .mondayId(1)
            .docSpaceId("aaa-aaa-aaa")
            .hash("someHash")
            .build();

    var response =
        mvc.perform(
                post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonUser.write(registerUser).getJson()))
            .andReturn()
            .getResponse();

    Mockito.verify(userCommandService, Mockito.times(0)).register(any(RegisterUser.class));
    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
