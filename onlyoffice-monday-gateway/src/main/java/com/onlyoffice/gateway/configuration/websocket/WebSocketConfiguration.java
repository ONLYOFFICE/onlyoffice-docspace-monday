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
package com.onlyoffice.gateway.configuration.websocket;

import com.onlyoffice.common.client.notification.factory.NotificationProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class WebSocketConfiguration implements WebSocketConfigurer {
  @Value("${server.origin}")
  private String selfOrigin;

  private final WebSocketHandler webSocketHandler;

  public WebSocketConfiguration(NotificationProcessor notificationProcessor) {
    if (WebSocketHandler.class.isAssignableFrom(notificationProcessor.getClass())) {
      this.webSocketHandler = (WebSocketHandler) notificationProcessor;
      return;
    }
    throw new WebSocketProcessorException(
        "Could not find any suitable NotificationProcessor for WebSocketConfiguration");
  }

  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
        .addHandler(webSocketHandler, "/notifications")
        .setAllowedOriginPatterns("https://*.monday.com", selfOrigin);
  }
}
