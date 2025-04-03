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
package com.onlyoffice.user.service.command;

import static org.assertj.core.api.Assertions.*;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.common.user.transfer.request.command.RemoveTenantUsers;
import com.onlyoffice.user.persistence.entity.User;
import com.onlyoffice.user.persistence.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BasicUserCommandServiceTest.TestConfig.class})
public class BasicUserCommandServiceTest {
  @MockBean private PlatformTransactionManager platformTransactionManager;
  @MockBean private UserRepository userRepository;
  @MockBean private CacheManager cacheManager;
  @Autowired private UserCommandService service;

  @TestConfiguration
  @Import(BasicUserCommandService.class)
  @ImportAutoConfiguration(ValidationAutoConfiguration.class)
  static class TestConfig {
    @Bean
    public static MethodValidationPostProcessor methodValidationPostProcessor() {
      MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
      processor.setProxyTargetClass(true);
      return processor;
    }

    @Bean
    public static DataSourceAutoConfiguration excludeDataSourceAutoConfiguration() {
      return new DataSourceAutoConfiguration();
    }
  }

  @Nested
  class RegisterUserTests {
    @ParameterizedTest
    @MethodSource("validRegisterUserCommandGenerator")
    void shouldRegisterNewUser_WhenUserDoesNotExist(RegisterUser command) {
      Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

      service.register(command);

      ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
      Mockito.verify(userRepository).save(captor.capture());
      User savedUser = captor.getValue();

      assertThat(savedUser.getTenantId()).isEqualTo(command.getTenantId());
      assertThat(savedUser.getMondayId()).isEqualTo(command.getMondayId());
      assertThat(savedUser.getDocSpaceId()).isEqualTo(command.getDocSpaceId());
      assertThat(savedUser.getEmail()).isEqualTo(command.getEmail());
      assertThat(savedUser.getHash()).isEqualTo(command.getHash());
    }

    @ParameterizedTest
    @MethodSource("invalidRegisterUserCommandGenerator")
    void shouldFailRegistrationNewUser_WhenUserInvalid(RegisterUser command) {
      Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.register(command))
          .isInstanceOf(ConstraintViolationException.class);
    }

    static Stream<RegisterUser> validRegisterUserCommandGenerator() {
      return Stream.of(
          RegisterUser.builder()
              .tenantId(1)
              .mondayId(1)
              .docSpaceId("aaa-aaa-aaa-aaa")
              .email("testone@example.com")
              .hash("mock")
              .build(),
          RegisterUser.builder()
              .tenantId(2)
              .mondayId(2)
              .docSpaceId("bbb-bbb-bbb-bbb")
              .email("testtwo@example.com")
              .hash("mock2")
              .build());
    }

    static Stream<RegisterUser> invalidRegisterUserCommandGenerator() {
      return Stream.of(
          null,
          RegisterUser.builder().build(),
          RegisterUser.builder()
              .mondayId(-1)
              .tenantId(1)
              .docSpaceId("bbb-bbb-bbb-bbb")
              .email("test@example.com")
              .hash("mock")
              .build(),
          RegisterUser.builder()
              .mondayId(1)
              .tenantId(-1)
              .docSpaceId("bbb-bbb-bbb-bbb")
              .email("test@example.com")
              .hash("mock")
              .build(),
          RegisterUser.builder()
              .mondayId(1)
              .tenantId(1)
              .docSpaceId("bbb-bbb-bbb-bbb")
              .email("notmail")
              .hash("mock")
              .build());
    }
  }

  @Nested
  class RemoveUserTests {
    @Test
    void shouldRemoveAllUsers() {
      assertThatNoException()
          .isThrownBy(
              () ->
                  service.removeAll(
                      CommandMessage.<RemoveTenantUsers>builder()
                          .payload(RemoveTenantUsers.builder().tenantId(1).build())
                          .commandAt(Instant.now().toEpochMilli())
                          .build()));
    }

    @Test
    void shouldThrowRemoveAllUsers_WhenInvalidCommand() {
      assertThatThrownBy(
              () -> service.removeAll(CommandMessage.<RemoveTenantUsers>builder().build()))
          .isInstanceOf(ConstraintViolationException.class);
    }
  }
}
