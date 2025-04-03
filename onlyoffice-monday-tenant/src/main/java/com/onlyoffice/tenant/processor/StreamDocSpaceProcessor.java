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
package com.onlyoffice.tenant.processor;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.client.notification.factory.NotificationPublisherFactory;
import com.onlyoffice.common.client.notification.transfer.event.AccessKeyRefreshed;
import com.onlyoffice.common.client.notification.transfer.event.NotificationEvent;
import com.onlyoffice.common.tenant.transfer.request.command.InviteRoomUsers;
import com.onlyoffice.common.tenant.transfer.request.command.RefreshAccessKey;
import com.onlyoffice.tenant.service.remote.BasicDocSpaceRemoteRoomService;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StreamDocSpaceProcessor implements DocSpaceProcessor {
  private final BasicDocSpaceRemoteRoomService service;
  private final Consumer<NotificationEvent> messagePublisher;

  public StreamDocSpaceProcessor(
      BasicDocSpaceRemoteRoomService service, NotificationPublisherFactory publisherFactory) {
    this.service = service;
    this.messagePublisher = publisherFactory.getPublisher("notifications");
  }

  @Bean
  public Consumer<CommandMessage<RefreshAccessKey>> refreshKeyConsumer() {
    return (msg) -> {
      var event = service.refreshAccessKey(msg);
      log.info("Sending an access key refreshed notification");
      messagePublisher.accept(
          AccessKeyRefreshed.builder()
              .tenantId(event.getTenantId())
              .boardId(event.getBoardId())
              .build());
    };
  }

  @Bean
  public Consumer<CommandMessage<InviteRoomUsers>> inviteUsersConsumer() {
    return service::inviteUsers;
  }
}
