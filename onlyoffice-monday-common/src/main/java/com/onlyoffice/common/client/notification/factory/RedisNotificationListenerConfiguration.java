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
package com.onlyoffice.common.client.notification.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@AutoConfiguration
@AutoConfigureAfter(RedisNotificationPublisherFactory.class)
@ConditionalOnBean({RedisConnectionFactory.class, NotificationProcessor.class})
public class RedisNotificationListenerConfiguration {
  @Value("${redis.notification.channel:notifications}")
  private String channel;

  @Bean
  public RedisMessageListenerContainer listenerContainer(
      RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
    var container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listenerAdapter, new PatternTopic(channel));
    return container;
  }

  @Bean
  public MessageListenerAdapter listenerAdapter(NotificationProcessor processor) {
    return new MessageListenerAdapter(processor, "onMessage");
  }
}
