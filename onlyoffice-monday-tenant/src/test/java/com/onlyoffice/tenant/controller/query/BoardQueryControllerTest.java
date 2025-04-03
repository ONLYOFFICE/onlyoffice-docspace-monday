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
package com.onlyoffice.tenant.controller.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.tenant.transfer.request.query.FindEntity;
import com.onlyoffice.common.tenant.transfer.response.BoardInformation;
import com.onlyoffice.tenant.controller.GlobalControllerAdvice;
import com.onlyoffice.tenant.service.query.BoardQueryService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
public class BoardQueryControllerTest {
  private MockMvc mvc;
  @Mock private BoardQueryService queryService;
  @InjectMocks private BoardQueryController controller;

  private JacksonTester<BoardInformation> jsonBoardInformation;

  @BeforeEach
  public void setup() {
    JacksonTester.initFields(this, new ObjectMapper());
    mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalControllerAdvice())
            .setValidator(new LocalValidatorFactoryBean())
            .build();
  }

  @Test
  void shouldFindBoard_WhenValidBoardId_ThenReturnBoardInformation() throws Exception {
    var boardId = 1;
    var boardInformation =
        BoardInformation.builder()
            .id(boardId)
            .roomId(100)
            .tenantId(10)
            .accessKey("accessKey")
            .build();

    Mockito.when(queryService.find(Mockito.any(FindEntity.class))).thenReturn(boardInformation);

    var response =
        mvc.perform(get("/tenants/boards/{boardId}", boardId).accept(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();

    Mockito.verify(queryService).find(Mockito.argThat(findEntity -> findEntity.getId() == boardId));

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

    var responseBody = jsonBoardInformation.parseObject(response.getContentAsString());

    assertThat(responseBody).isNotNull();
    assertThat(responseBody.getId()).isEqualTo(boardInformation.getId());
    assertThat(responseBody.getRoomId()).isEqualTo(boardInformation.getRoomId());
    assertThat(responseBody.getTenantId()).isEqualTo(boardInformation.getTenantId());
    assertThat(responseBody.getAccessKey()).isEqualTo(boardInformation.getAccessKey());
  }

  @Test
  void shouldReturnNotFound_WhenBoardDoesNotExist() throws Exception {
    var boardId = 1;

    Mockito.when(queryService.find(Mockito.any(FindEntity.class))).thenReturn(null);

    var response =
        mvc.perform(get("/tenants/boards/{boardId}", boardId).accept(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();

    Mockito.verify(queryService).find(Mockito.argThat(findEntity -> findEntity.getId() == boardId));
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo("");
  }
}
