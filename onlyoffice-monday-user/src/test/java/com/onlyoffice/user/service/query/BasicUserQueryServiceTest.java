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
package com.onlyoffice.user.service.query;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.onlyoffice.common.user.transfer.request.query.FindUser;
import com.onlyoffice.user.exception.UserNotFoundException;
import com.onlyoffice.user.persistence.entity.User;
import com.onlyoffice.user.persistence.entity.UserId;
import com.onlyoffice.user.persistence.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BasicUserQueryServiceTest.TestConfig.class})
public class BasicUserQueryServiceTest {
  @MockBean private UserRepository userRepository;
  @Autowired private UserQueryService service;

  @TestConfiguration
  @Import(BasicUserQueryService.class)
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
  class FindMondayUsersTests {
    @Test
    void shouldRequestMondayUser_WhenExists_ThenReturn() {
      Mockito.when(userRepository.findById(UserId.builder().mondayId(1).tenantId(1).build()))
          .thenReturn(
              Optional.of(
                  User.builder()
                      .mondayId(1)
                      .tenantId(1)
                      .hash("hash")
                      .email("test@email.com")
                      .docSpaceId("aaa-aaa-aaa")
                      .createdAt(Instant.now().toEpochMilli())
                      .updatedAt(Instant.now().toEpochMilli())
                      .version(1)
                      .build()));

      assertThatNoException()
          .isThrownBy(() -> service.findUser(FindUser.builder().mondayId(1).tenantId(1).build()));
    }

    @Test
    void shouldRequestMondayUser_WhenDoesNotExist_ThenReturnEmpty() {
      assertThatException()
          .isThrownBy(() -> service.findUser(FindUser.builder().tenantId(1).mondayId(1).build()))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldRequestMondayUser_WhenQueryNotValid_ThenThrowException() {
      assertThatException()
          .isThrownBy(() -> service.findUser(FindUser.builder().build()))
          .isInstanceOf(ConstraintViolationException.class);
    }
  }
}
