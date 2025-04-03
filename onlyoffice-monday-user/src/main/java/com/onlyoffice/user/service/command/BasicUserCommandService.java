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
package com.onlyoffice.user.service.command;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.common.user.transfer.request.command.RemoveTenantUsers;
import com.onlyoffice.user.persistence.entity.User;
import com.onlyoffice.user.persistence.entity.UserId;
import com.onlyoffice.user.persistence.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BasicUserCommandService implements UserCommandService {
  private final PlatformTransactionManager platformTransactionManager;
  private final UserRepository userRepository;
  private final CacheManager cacheManager;

  @CacheEvict(value = "users", key = "#payload.tenantId+#payload.mondayId")
  @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public void register(@Valid @NotNull RegisterUser payload) {
    try {
      var now = System.currentTimeMillis();
      MDC.put("tenant_id", String.valueOf(payload.getTenantId()));
      MDC.put("monday_id", String.valueOf(payload.getMondayId()));
      MDC.put("docSpace_id", String.valueOf(payload.getDocSpaceId()));
      log.info("Registering a new user set of credentials");

      userRepository
          .findById(
              UserId.builder()
                  .mondayId(payload.getMondayId())
                  .tenantId(payload.getTenantId())
                  .build())
          .ifPresentOrElse(
              u -> {
                u.setDocSpaceId(payload.getDocSpaceId());
                u.setEmail(payload.getEmail());
                u.setHash(payload.getHash());
                u.setUpdatedAt(now);
                userRepository.save(u);
              },
              () ->
                  userRepository.save(
                      User.builder()
                          .mondayId(payload.getMondayId())
                          .tenantId(payload.getTenantId())
                          .docSpaceId(payload.getDocSpaceId())
                          .email(payload.getEmail())
                          .hash(payload.getHash())
                          .createdAt(now)
                          .updatedAt(now)
                          .build()));
    } finally {
      MDC.clear();
    }
  }

  @Retryable(
      retryFor = {Exception.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 2000, multiplier = 1.5))
  public void register(@Valid @NotNull CommandMessage<RegisterUser> command) {
    try {
      var payload = command.getPayload();
      MDC.put("tenant_id", String.valueOf(payload.getTenantId()));
      MDC.put("monday_id", String.valueOf(payload.getMondayId()));
      MDC.put("docSpace_id", String.valueOf(payload.getDocSpaceId()));
      log.info("Registering a new user set of credentials asynchronously");

      var template = new TransactionTemplate(platformTransactionManager);
      template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      template.setIsolationLevel(Isolation.REPEATABLE_READ.value());
      template.setTimeout(1);

      template.execute(
          status -> {
            try {
              userRepository
                  .findById(
                      UserId.builder()
                          .mondayId(payload.getMondayId())
                          .tenantId(payload.getTenantId())
                          .build())
                  .ifPresentOrElse(
                      u -> {
                        if (u.getUpdatedAt().compareTo(command.getCommandAt()) < 0) {
                          u.setDocSpaceId(payload.getDocSpaceId());
                          u.setEmail(payload.getEmail());
                          u.setHash(payload.getHash());
                          u.setUpdatedAt(command.getCommandAt());
                          userRepository.save(u);
                        }
                      },
                      () ->
                          userRepository.save(
                              User.builder()
                                  .mondayId(payload.getMondayId())
                                  .tenantId(payload.getTenantId())
                                  .docSpaceId(payload.getDocSpaceId())
                                  .email(payload.getEmail())
                                  .hash(payload.getHash())
                                  .createdAt(command.getCommandAt())
                                  .updatedAt(command.getCommandAt())
                                  .build()));
              return true;
            } catch (Exception e) {
              status.setRollbackOnly();
              throw e;
            }
          });

    } finally {
      MDC.clear();
    }
  }

  @Retryable(
      retryFor = {Exception.class},
      maxAttempts = 7,
      backoff = @Backoff(delay = 2000, multiplier = 1.5))
  public void removeAll(@Valid @NotNull CommandMessage<RemoveTenantUsers> command) {
    try {
      var payload = command.getPayload();
      MDC.put("tenant_id", String.valueOf(payload.getTenantId()));
      log.info("Removing all tenant users");

      var cache = cacheManager.getCache("users");
      if (cache != null) cache.clear();

      var template = new TransactionTemplate(platformTransactionManager);
      template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      template.setTimeout(2);
      template.execute(
          status -> {
            try {
              userRepository.deleteAllByTenantIdAndUpdatedAtLessThanEqual(
                  payload.getTenantId(), command.getCommandAt());
              return true;
            } catch (Exception e) {
              status.setRollbackOnly();
              throw e;
            }
          });
    } finally {
      MDC.clear();
    }
  }
}
