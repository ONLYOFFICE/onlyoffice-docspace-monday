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
import com.onlyoffice.common.tenant.transfer.response.TenantCredentials;
import com.onlyoffice.tenant.controller.GlobalControllerAdvice;
import com.onlyoffice.tenant.service.query.TenantQueryService;
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
public class TenantQueryControllerTest {
  private MockMvc mvc;
  @Mock private TenantQueryService queryService;
  @InjectMocks private TenantQueryController controller;

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
  void shouldFindTenant_WhenValidTenantId_ThenReturnTenantCredentials() throws Exception {
    var tenantId = 1;
    var tenantCredentials =
        TenantCredentials.builder()
            .id(tenantId)
            .docSpaceUrl("https://example.com")
            .docSpaceLogin("admin@admin.com")
            .docSpaceHash("hash")
            .build();

    Mockito.when(queryService.find(Mockito.any(FindEntity.class))).thenReturn(tenantCredentials);

    var response =
        mvc.perform(get("/tenants/{tenantId}", tenantId).accept(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();

    Mockito.verify(queryService)
        .find(Mockito.argThat(findEntity -> findEntity.getId() == tenantId));
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
