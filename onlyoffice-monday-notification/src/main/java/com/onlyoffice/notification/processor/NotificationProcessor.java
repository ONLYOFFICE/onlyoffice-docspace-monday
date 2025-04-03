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
package com.onlyoffice.notification.processor;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessor {
  private final String TRACE_HEADER = "traceparent";
  private final String userTemplate =
      """
    ⚠️ Warning ⚠️
    The service could not create a new user.

    TraceID: %s
    """;
  private final String invitationTemplate =
      """
    ⚠️ Warning ⚠️
    The service could not invite users.

    TraceID: %s
    """;

  private final ProducerTemplate producerTemplate;

  @Bean
  public Consumer<Message<?>> badUserUpsertConsumer() {
    return msg -> {
      var header = msg.getHeaders().get(TRACE_HEADER);
      var trace = header == null ? "no trace_id" : header.toString();
      producerTemplate.sendBody("direct:sendTelegram", String.format(userTemplate, trace));
    };
  }

  @Bean
  public Consumer<Message<?>> badInviteUsersConsumer() {
    return msg -> {
      var header = msg.getHeaders().get(TRACE_HEADER);
      var trace = header == null ? "no trace_id" : header.toString();
      producerTemplate.sendBody("direct:sendTelegram", String.format(invitationTemplate, trace));
    };
  }
}
