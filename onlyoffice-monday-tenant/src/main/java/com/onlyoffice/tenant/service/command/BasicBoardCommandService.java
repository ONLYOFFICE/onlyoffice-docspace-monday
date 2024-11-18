package com.onlyoffice.tenant.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.tenant.transfer.request.command.InviteRoomUsers;
import com.onlyoffice.common.tenant.transfer.request.command.RefreshAccessKey;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterRoom;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveRoom;
import com.onlyoffice.tenant.exception.OutboxSerializationException;
import com.onlyoffice.tenant.persistence.entity.Board;
import com.onlyoffice.tenant.persistence.entity.Outbox;
import com.onlyoffice.tenant.persistence.entity.OutboxType;
import com.onlyoffice.tenant.persistence.repository.BoardRepository;
import com.onlyoffice.tenant.persistence.repository.OutboxRepository;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBoardCommandService implements BoardCommandService {
  private final ObjectMapper mapper;

  private final OutboxRepository outboxRepository;
  private final BoardRepository boardRepository;
  private final TenantRepository tenantRepository;

  @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public void register(RegisterRoom command, Set<String> docSpaceUsers) {
    try {
      MDC.put("tenant_id", String.valueOf(command.getTenantId()));
      MDC.put("board_id", String.valueOf(command.getBoardId()));
      MDC.put("room_id", String.valueOf(command.getRoomId()));
      log.info("Registering a board room");

      var now = System.currentTimeMillis();
      boardRepository
          .findById(command.getBoardId())
          .ifPresentOrElse(
              board -> board.setRoomId(command.getRoomId()),
              () -> {
                boardRepository.save(
                    Board.builder()
                        .id(command.getBoardId())
                        .roomId(command.getRoomId())
                        .tenant(tenantRepository.getReferenceById(command.getTenantId()))
                        .build());
              });
      outboxRepository.save(
          Outbox.builder()
              .payload(
                  mapper.writeValueAsString(
                      CommandMessage.<RefreshAccessKey>builder()
                          .payload(RefreshAccessKey.builder().boardId(command.getBoardId()).build())
                          .commandAt(now)
                          .build()))
              .type(OutboxType.REFRESH)
              .build());
      outboxRepository.save(
          Outbox.builder()
              .payload(
                  mapper.writeValueAsString(
                      CommandMessage.<InviteRoomUsers>builder()
                          .payload(
                              InviteRoomUsers.builder()
                                  .tenantId(command.getTenantId())
                                  .roomId(command.getRoomId())
                                  .docSpaceUsers(docSpaceUsers)
                                  .build())
                          .commandAt(now)
                          .build()))
              .type(OutboxType.INVITE)
              .build());
    } catch (JsonProcessingException e) {
      log.error("Could not perform json serialization", e);
      throw new OutboxSerializationException(e);
    } finally {
      MDC.clear();
    }
  }

  @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
  public void remove(RemoveRoom command) {
    try {
      MDC.put("tenant_id", String.valueOf(command.getTenantId()));
      MDC.put("board_id", String.valueOf(command.getBoardId()));
      log.info("Unlinking room from a board");
      boardRepository.deleteById(command.getBoardId());
    } finally {
      MDC.clear();
    }
  }
}
