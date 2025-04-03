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
package com.onlyoffice.user.processor;

import static org.assertj.core.api.Assertions.assertThatNoException;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.common.user.transfer.request.command.RemoveTenantUsers;
import com.onlyoffice.user.service.command.UserCommandService;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserProcessorTest {
  @Mock UserCommandService userCommandService;
  @InjectMocks UserProcessor processor;

  @Nested
  class UserProcessorRegisterTests {
    @Test
    void shouldProcessRegisterUser_WhenValidCommand() {
      assertThatNoException()
          .isThrownBy(
              () ->
                  processor
                      .registerUserConsumer()
                      .accept(
                          CommandMessage.<RegisterUser>builder()
                              .commandAt(Instant.now().toEpochMilli())
                              .payload(
                                  RegisterUser.builder()
                                      .tenantId(1)
                                      .mondayId(1)
                                      .docSpaceId("aaa-aaa-aaa")
                                      .hash("hash")
                                      .email("test@email.com")
                                      .build())
                              .build()));
    }
  }

  @Nested
  class UserProcessorRemoveTests {
    @Test
    void shouldProcessRemoveUsers_WhenValidCommand() {
      assertThatNoException()
          .isThrownBy(
              () ->
                  processor
                      .removeTenantUsersConsumer()
                      .accept(
                          CommandMessage.<RemoveTenantUsers>builder()
                              .commandAt(Instant.now().toEpochMilli())
                              .payload(RemoveTenantUsers.builder().tenantId(1).build())
                              .build()));
    }
  }
}
