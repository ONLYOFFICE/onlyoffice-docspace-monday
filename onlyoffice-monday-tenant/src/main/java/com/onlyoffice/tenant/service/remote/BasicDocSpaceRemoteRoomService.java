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
package com.onlyoffice.tenant.service.remote;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.docspace.transfer.request.command.AuthenticateUser;
import com.onlyoffice.common.docspace.transfer.request.command.ChangeRoomAccess;
import com.onlyoffice.common.docspace.transfer.request.command.UserAccess;
import com.onlyoffice.common.service.encryption.EncryptionService;
import com.onlyoffice.common.tenant.transfer.event.AccessKeyRefreshed;
import com.onlyoffice.common.tenant.transfer.request.command.InviteRoomUsers;
import com.onlyoffice.common.tenant.transfer.request.command.RefreshAccessKey;
import com.onlyoffice.tenant.client.DocSpaceClient;
import com.onlyoffice.tenant.exception.BoardNotFoundException;
import com.onlyoffice.tenant.exception.OperationExecutionException;
import com.onlyoffice.tenant.exception.TenantNotFoundException;
import com.onlyoffice.tenant.persistence.repository.BoardRepository;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import feign.Feign;
import feign.Target;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class BasicDocSpaceRemoteRoomService implements DocSpaceRemoteRoomService {
  private final EncryptionService encryptionService;

  private final PlatformTransactionManager transactionManager;
  private final TenantRepository tenantRepository;
  private final BoardRepository boardRepository;
  private final CacheManager cacheManager;

  private final DocSpaceClient client;

  public BasicDocSpaceRemoteRoomService(
      EncryptionService encryptionService,
      TenantRepository tenantRepository,
      BoardRepository boardRepository,
      PlatformTransactionManager transactionManager,
      CacheManager cacheManager) {
    this.encryptionService = encryptionService;
    this.tenantRepository = tenantRepository;
    this.boardRepository = boardRepository;
    this.transactionManager = transactionManager;
    this.cacheManager = cacheManager;
    this.client =
        Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .target(Target.EmptyTarget.create(DocSpaceClient.class));
  }

  public AccessKeyRefreshed refreshAccessKey(CommandMessage<RefreshAccessKey> command) {
    try {
      var payload = command.getPayload();
      var boardsCache = cacheManager.getCache("boards");
      if (boardsCache != null) boardsCache.evict(payload.getBoardId());

      MDC.put("board_id", String.valueOf(payload.getBoardId()));
      log.info("Refreshing access key for current board");

      var board =
          boardRepository
              .findById(payload.getBoardId())
              .orElseThrow(
                  () ->
                      new BoardNotFoundException(
                          String.format("Could not find board with id %d", payload.getBoardId())));
      var docspace = board.getTenant().getDocspace();
      var url = URI.create(docspace.getUrl());
      var accessKeyResult =
          CompletableFuture.supplyAsync(
                  () ->
                      client.generateToken(
                          url,
                          AuthenticateUser.builder()
                              .userName(docspace.getAdminLogin())
                              .password(encryptionService.decrypt(docspace.getAdminHash()))
                              .build()))
              .thenApply(
                  token ->
                      Optional.ofNullable(
                          client
                              .generateSharedKey(
                                  url, board.getRoomId(), token.getResponse().getToken())
                              .getResponse()))
              .exceptionally((ex) -> Optional.empty())
              .get(5, TimeUnit.SECONDS);

      var accessKey =
          accessKeyResult
              .orElseThrow(
                  () ->
                      new OperationExecutionException(
                          "Could not get an accessKey response from DocSpace"))
              .getFirst()
              .getSharedTo()
              .getRequestToken();
      var template = new TransactionTemplate(transactionManager);
      template.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
      template.setTimeout(2);
      template.execute(
          status -> {
            boardRepository.getReferenceById(payload.getBoardId()).setAccessKey(accessKey);
            return status;
          });
      return AccessKeyRefreshed.builder()
          .tenantId(board.getTenant().getId())
          .boardId(payload.getBoardId())
          .build();
    } catch (Exception e) {
      log.warn("Could not refresh access key", e);
      throw new OperationExecutionException(e);
    } finally {
      MDC.clear();
    }
  }

  public void inviteUsers(CommandMessage<InviteRoomUsers> command) {
    try {
      var payload = command.getPayload();

      MDC.put("tenant_id", String.valueOf(payload.getTenantId()));
      MDC.put("room_id", String.valueOf(payload.getRoomId()));
      log.info("Inviting users to DocSpace");

      var tenant =
          tenantRepository
              .findById(payload.getTenantId())
              .orElseThrow(
                  () ->
                      new TenantNotFoundException(
                          String.format("Could not find tenant with id %d", payload.getRoomId())));
      var docspace = tenant.getDocspace();
      var url = URI.create(docspace.getUrl());
      CompletableFuture.supplyAsync(
              () ->
                  client.generateToken(
                      url,
                      AuthenticateUser.builder()
                          .userName(docspace.getAdminLogin())
                          .password(encryptionService.decrypt(docspace.getAdminHash()))
                          .build()))
          .thenAccept(
              token ->
                  client.changeRoomAccess(
                      url,
                      payload.getRoomId(),
                      token.getResponse().getToken(),
                      ChangeRoomAccess.builder()
                          .invitations(
                              payload.getDocSpaceUsers().stream()
                                  .map(
                                      u ->
                                          ChangeRoomAccess.AccessEntry.builder()
                                              .id(u)
                                              .access(UserAccess.POWER_USER)
                                              .build())
                                  .collect(Collectors.toSet()))
                          .notify(true)
                          .build()))
          .get(5, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.warn("Could not invite users to DocSpace", e);
      throw new OperationExecutionException(e);
    } finally {
      MDC.clear();
    }
  }
}
