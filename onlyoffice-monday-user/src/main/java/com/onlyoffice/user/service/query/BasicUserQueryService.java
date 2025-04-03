/**
 * (c) Copyright Ascensio System SIA 2025
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

import com.onlyoffice.common.user.transfer.request.query.FindDocSpaceUsers;
import com.onlyoffice.common.user.transfer.request.query.FindUser;
import com.onlyoffice.common.user.transfer.response.DocSpaceUsers;
import com.onlyoffice.common.user.transfer.response.UserCredentials;
import com.onlyoffice.user.exception.ExecutionTimeoutException;
import com.onlyoffice.user.exception.UserNotFoundException;
import com.onlyoffice.user.persistence.entity.User;
import com.onlyoffice.user.persistence.entity.UserId;
import com.onlyoffice.user.persistence.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BasicUserQueryService implements UserQueryService {
  private final UserRepository userRepository;

  @Cacheable(value = "users", key = "#query.tenantId+#query.mondayId", unless = "#result == null")
  public UserCredentials findUser(@Valid FindUser query) {
    try {
      MDC.put("tenant_id", String.valueOf(query.getTenantId()));
      MDC.put("monday_id", String.valueOf(query.getMondayId()));
      log.info("Trying to find tenant user by id");

      var user =
          userRepository
              .findById(
                  UserId.builder()
                      .mondayId(query.getMondayId())
                      .tenantId(query.getTenantId())
                      .build())
              .orElseThrow(
                  () ->
                      new UserNotFoundException(
                          String.format(
                              "Could not find user %d for tenant %d",
                              query.getTenantId(), query.getMondayId())));

      return UserCredentials.builder().email(user.getEmail()).hash(user.getHash()).build();
    } finally {
      MDC.clear();
    }
  }

  @Cacheable(value = "users", key = "#query.tenantId+#query.mondayId", unless = "#result == null")
  public UserCredentials findUser(@Valid FindUser query, @Positive int timeout) {
    var leastTimeout = Math.min(timeout, 3500);
    try {
      MDC.put("tenant_id", String.valueOf(query.getTenantId()));
      MDC.put("monday_id", String.valueOf(query.getMondayId()));
      MDC.put("timeout", String.valueOf(timeout));
      log.info("Trying to find tenant user by id with timeout");

      return CompletableFuture.supplyAsync(
              () -> {
                var user =
                    userRepository
                        .findByIdWithTimeout(
                            UserId.builder()
                                .mondayId(query.getMondayId())
                                .tenantId(query.getTenantId())
                                .build(),
                            timeout)
                        .orElseThrow(
                            () ->
                                new UserNotFoundException(
                                    String.format(
                                        "Could not find user %d for tenant %d",
                                        query.getTenantId(), query.getMondayId())));
                return UserCredentials.builder()
                    .email(user.getEmail())
                    .hash(user.getHash())
                    .build();
              })
          .get(leastTimeout, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof UserNotFoundException) throw (UserNotFoundException) e.getCause();
      throw new ExecutionTimeoutException(e);
    } catch (InterruptedException | TimeoutException e) {
      log.error("Could not find tenant user by id", e);
      throw new ExecutionTimeoutException(e);
    } finally {
      MDC.clear();
    }
  }

  public DocSpaceUsers findDocSpaceUsers(@Valid FindDocSpaceUsers query) {
    try {
      MDC.put("tenant_id", String.valueOf(query.getTenantId()));
      log.info("Trying to find DocSpace users by tenant_id");

      var ids =
          query.getMondayIds().stream()
              .map(id -> UserId.builder().mondayId(id).tenantId(query.getTenantId()).build())
              .collect(Collectors.toSet());
      return DocSpaceUsers.builder()
          .ids(
              userRepository.findAllById(ids).stream()
                  .map(User::getDocSpaceId)
                  .collect(Collectors.toSet()))
          .build();
    } finally {
      MDC.clear();
    }
  }

  public DocSpaceUsers findDocSpaceUsers(@Valid FindDocSpaceUsers query, @Positive int timeout) {
    try {
      MDC.put("tenant_id", String.valueOf(query.getTenantId()));
      MDC.put("timeout", String.valueOf(timeout));
      log.info("Trying to find DocSpace users by tenant_id with timeout");

      var ids =
          query.getMondayIds().stream()
              .map(id -> UserId.builder().mondayId(id).tenantId(query.getTenantId()).build())
              .collect(Collectors.toSet());
      return DocSpaceUsers.builder()
          .ids(
              userRepository.findAllByIdsWithTimeout(ids, timeout).stream()
                  .map(User::getDocSpaceId)
                  .collect(Collectors.toSet()))
          .build();
    } finally {
      MDC.clear();
    }
  }
}
