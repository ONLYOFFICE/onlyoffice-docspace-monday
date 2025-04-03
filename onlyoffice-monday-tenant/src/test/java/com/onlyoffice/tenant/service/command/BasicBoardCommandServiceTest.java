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
package com.onlyoffice.tenant.service.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterRoom;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveRoom;
import com.onlyoffice.tenant.exception.OutboxSerializationException;
import com.onlyoffice.tenant.persistence.entity.Board;
import com.onlyoffice.tenant.persistence.entity.Outbox;
import com.onlyoffice.tenant.persistence.entity.OutboxType;
import com.onlyoffice.tenant.persistence.entity.Tenant;
import com.onlyoffice.tenant.persistence.repository.BoardRepository;
import com.onlyoffice.tenant.persistence.repository.OutboxRepository;
import com.onlyoffice.tenant.persistence.repository.TenantRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
public class BasicBoardCommandServiceTest {
  @Mock private ObjectMapper mapper;
  @Mock private OutboxRepository outboxRepository;
  @Mock private BoardRepository boardRepository;
  @Mock private TenantRepository tenantRepository;
  @InjectMocks private BasicBoardCommandService service;

  @BeforeEach
  public void setup() {
    MDC.clear();
  }

  @Nested
  class RegistrationCommandTests {
    @Test
    void register_ShouldUpdateExistingBoard_WhenBoardExists() throws Exception {
      var docSpaceUsers = Set.of("user1", "user2");
      var command = RegisterRoom.builder().tenantId(1).boardId(1).roomId(2).build();
      var existingBoard =
          Board.builder().id(1).roomId(1).tenant(Tenant.builder().id(1).build()).build();

      when(boardRepository.findById(command.getBoardId())).thenReturn(Optional.of(existingBoard));

      service.register(command, docSpaceUsers);

      assertThat(existingBoard.getRoomId()).isEqualTo(command.getRoomId());
      verify(boardRepository, never()).save(any(Board.class));
      verify(outboxRepository, times(2)).save(any(Outbox.class));
    }

    @Test
    void register_ShouldSaveNewBoard_WhenBoardDoesNotExist() throws Exception {
      var docSpaceUsers = Set.of("user1", "user2");
      var command = RegisterRoom.builder().tenantId(1).boardId(1).roomId(1).build();

      when(boardRepository.findById(command.getBoardId())).thenReturn(Optional.empty());
      when(tenantRepository.getReferenceById(command.getTenantId()))
          .thenReturn(Tenant.builder().id(1).build());

      when(mapper.writeValueAsString(any())).thenReturn("mock-payload");
      service.register(command, docSpaceUsers);

      verify(boardRepository)
          .save(
              argThat(
                  board ->
                      board.getId() == command.getBoardId()
                          && board.getRoomId() == command.getRoomId()
                          && board.getTenant() != null));
      verify(outboxRepository, times(2)).save(any(Outbox.class));
      verify(outboxRepository).save(argThat(outbox -> outbox.getType() == OutboxType.REFRESH));
      verify(outboxRepository).save(argThat(outbox -> outbox.getType() == OutboxType.INVITE));
    }

    @Test
    void register_ShouldThrowOutboxSerializationException_WhenSerializationFails()
        throws Exception {
      var docSpaceUsers = Set.of("user1", "user2");
      var command = RegisterRoom.builder().tenantId(1).boardId(1).roomId(1).build();

      when(boardRepository.findById(command.getBoardId())).thenReturn(Optional.empty());
      when(tenantRepository.getReferenceById(command.getTenantId()))
          .thenReturn(Tenant.builder().id(1).build());

      when(mapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

      assertThatThrownBy(() -> service.register(command, docSpaceUsers))
          .isInstanceOf(OutboxSerializationException.class);

      verify(boardRepository).save(any(Board.class));
      verify(outboxRepository, never()).save(any(Outbox.class));
    }
  }

  @Nested
  class RemoveCommandTests {
    @Test
    void remove_ShouldDeleteBoardById() {
      var command = RemoveRoom.builder().tenantId(1).boardId(1).build();
      service.remove(command);
      verify(boardRepository).deleteById(command.getBoardId());
    }
  }
}
