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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.user.transfer.request.query.FindUser;
import com.onlyoffice.common.user.transfer.response.DocSpaceUsers;
import com.onlyoffice.common.user.transfer.response.UserCredentials;
import com.onlyoffice.user.exception.UserNotFoundException;
import com.onlyoffice.user.service.query.UserQueryService;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class UserQueryControllerTest {
  private MockMvc mvc;
  @Mock private UserQueryService userQueryService;
  @InjectMocks private UserQueryController queryController;
  private JacksonTester<UserCredentials> jsonUserCredentials;
  private JacksonTester<DocSpaceUsers> jsonDocSpaceUsers;

  private final int tenantId = 1;
  private final int mondayId = 1;
  private final int timeout = 3500;

  @BeforeEach
  public void setup() {
    JacksonTester.initFields(this, new ObjectMapper());
    mvc =
        MockMvcBuilders.standaloneSetup(queryController)
            .setControllerAdvice(new GlobalControllerAdvice())
            .build();
  }

  @Test
  void shouldFindUser_WhenValidRequest_ThenReturnUserCredentials() throws Exception {
    var userCredentials =
        UserCredentials.builder().email("test@example.com").hash("someHash").build();
    var expectedJson = jsonUserCredentials.write(userCredentials).getJson();

    Mockito.when(userQueryService.findUser(any(FindUser.class), eq(timeout)))
        .thenReturn(userCredentials);

    var response =
        mvc.perform(
                get("/users/{tenantId}/{mondayId}", tenantId, mondayId)
                    .header("X-Timeout", String.valueOf(timeout)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();

    assertThat(response.getContentAsString()).isEqualToIgnoringWhitespace(expectedJson);
    Mockito.verify(userQueryService, Mockito.times(1)).findUser(any(), eq(timeout));
  }

  @Test
  void shouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
    Mockito.when(userQueryService.findUser(any(FindUser.class), eq(timeout)))
        .thenThrow(new UserNotFoundException("User not found"));

    mvc.perform(
            get("/users/{tenantId}/{mondayId}", tenantId, 999)
                .header("X-Timeout", String.valueOf(timeout)))
        .andExpect(status().isNotFound());

    Mockito.verify(userQueryService, Mockito.times(1)).findUser(any(FindUser.class), eq(timeout));
  }

  @Test
  void shouldFindDocSpaceUsers_WhenValidRequest_ThenReturnDocSpaceUsers() throws Exception {
    var ids = Set.of(1, 2, 3);
    var docSpaceUsers =
        DocSpaceUsers.builder().ids(Set.of("docSpaceId1", "docSpaceId2", "docSpaceId3")).build();
    var expectedJson = jsonDocSpaceUsers.write(docSpaceUsers).getJson();

    Mockito.when(userQueryService.findDocSpaceUsers(any(), eq(timeout))).thenReturn(docSpaceUsers);

    var response =
        mvc.perform(
                get("/users/{tenantId}", tenantId)
                    .param("id", "1", "2", "3")
                    .header("X-Timeout", String.valueOf(timeout)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();

    assertThat(response.getContentAsString()).isEqualToIgnoringWhitespace(expectedJson);
    Mockito.verify(userQueryService, Mockito.times(1)).findDocSpaceUsers(any(), eq(timeout));
  }

  @Test
  void shouldReturnBadRequest_WhenRequestParamMissing() throws Exception {
    mvc.perform(get("/users/{tenantId}", tenantId).header("X-Timeout", String.valueOf(timeout)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(userQueryService);
  }
}
