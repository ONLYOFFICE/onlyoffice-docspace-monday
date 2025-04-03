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

import com.onlyoffice.common.client.notification.transfer.event.NotificationEvent;
import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnBean({RedisConnectionFactory.class})
public class RedisNotificationPublisherFactory implements NotificationPublisherFactory {
  private final RedisTemplate<String, NotificationEvent> redisTemplate;

  public RedisNotificationPublisherFactory(RedisConnectionFactory redisConnectionFactory) {
    var template = new RedisTemplate<String, NotificationEvent>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new Jackson2JsonRedisSerializer<>(NotificationEvent.class));
    template.afterPropertiesSet();
    redisTemplate = template;
  }

  public <T extends NotificationEvent> Consumer<T> getPublisher(String channel) {
    return (T message) -> {
      redisTemplate.convertAndSend(channel, message);
    };
  }
}
