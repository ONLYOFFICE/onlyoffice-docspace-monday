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
package com.onlyoffice.tenant.processor;

import static org.mockito.Mockito.*;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.client.notification.factory.NotificationPublisherFactory;
import com.onlyoffice.common.client.notification.transfer.event.AccessKeyRefreshed;
import com.onlyoffice.common.client.notification.transfer.event.NotificationEvent;
import com.onlyoffice.common.tenant.transfer.request.command.InviteRoomUsers;
import com.onlyoffice.common.tenant.transfer.request.command.RefreshAccessKey;
import com.onlyoffice.tenant.service.remote.BasicDocSpaceRemoteRoomService;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamDocSpaceProcessorTest {
  @Mock private BasicDocSpaceRemoteRoomService service;
  @Mock private NotificationPublisherFactory publisherFactory;
  @Mock private Consumer<NotificationEvent> messagePublisher;
  @InjectMocks private StreamDocSpaceProcessor processor;

  @BeforeEach
  void setup() {
    when(publisherFactory.getPublisher("notifications")).thenReturn(messagePublisher);
    processor = new StreamDocSpaceProcessor(service, publisherFactory);
  }

  @Test
  void shouldRefreshKey_WhenEventReceived() {
    var payload = RefreshAccessKey.builder().boardId(1).build();
    var commandMessage = CommandMessage.<RefreshAccessKey>builder().payload(payload).build();

    var accessKeyRefreshedEvent =
        com.onlyoffice.common.tenant.transfer.event.AccessKeyRefreshed.builder()
            .tenantId(100)
            .boardId(1)
            .build();

    when(service.refreshAccessKey(commandMessage)).thenReturn(accessKeyRefreshedEvent);

    var consumer = processor.refreshKeyConsumer();
    consumer.accept(commandMessage);

    verify(service).refreshAccessKey(commandMessage);
    verify(messagePublisher)
        .accept(
            argThat(
                event ->
                    event instanceof AccessKeyRefreshed
                        && ((AccessKeyRefreshed) event).getTenantId()
                            == accessKeyRefreshedEvent.getTenantId()
                        && ((AccessKeyRefreshed) event).getBoardId()
                            == accessKeyRefreshedEvent.getBoardId()));
  }

  @Test
  void shouldInviteUsersConsumer_WhenEventReceived() {
    var payload =
        InviteRoomUsers.builder()
            .tenantId(1)
            .roomId(200)
            .docSpaceUsers(Set.of("user1", "user2"))
            .build();
    var commandMessage = CommandMessage.<InviteRoomUsers>builder().payload(payload).build();

    var consumer = processor.inviteUsersConsumer();
    consumer.accept(commandMessage);

    verify(service).inviteUsers(commandMessage);
    verifyNoInteractions(messagePublisher);
  }
}
