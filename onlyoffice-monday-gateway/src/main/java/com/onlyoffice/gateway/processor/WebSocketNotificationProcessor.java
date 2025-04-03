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
package com.onlyoffice.gateway.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.common.client.notification.factory.NotificationProcessor;
import com.onlyoffice.gateway.transport.websocket.SessionToken;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationProcessor extends TextWebSocketHandler
    implements MessageListener, NotificationProcessor {
  private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
  private final JwtDecoder jwtDecoder;
  private final ObjectMapper mapper;

  public void afterConnectionEstablished(@NotNull WebSocketSession session) {
    sessions.add(session);
  }

  public void afterConnectionClosed(
      @NotNull WebSocketSession session, @NotNull CloseStatus status) {
    try {
      setMdcForSession(session);
      log.debug("Removing websocket session from current instance registry");

      sessions.remove(session);
    } finally {
      MDC.clear();
    }
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    broadcast(new String(message.getBody()));
  }

  private void broadcast(String message) {
    var textMessage = new TextMessage(message);
    sessions.forEach(session -> processSessionForBroadcast(session, textMessage, message));
  }

  private void processSessionForBroadcast(
      WebSocketSession session, TextMessage textMessage, String message) {
    try {
      var tenantId = extractTenantId(message);
      if (tenantId != -1 && isSessionEligible(session, tenantId))
        sendNotification(session, textMessage, tenantId);
    } catch (Exception e) {
      log.error("Could not notify a session", e);
    } finally {
      MDC.clear();
    }
  }

  private int extractTenantId(String message) {
    try {
      return mapper.readTree(message).get("tenant_id").asInt();
    } catch (Exception e) {
      log.error("Failed to extract tenant ID from message", e);
      return -1;
    }
  }

  private boolean isSessionEligible(WebSocketSession session, long tenantId) {
    if (!session.isOpen() || session.getUri() == null) return false;
    var token = getSessionToken(session);
    if (token == null) return false;
    return verifyTenantId(token, tenantId);
  }

  private String getSessionToken(WebSocketSession session) {
    var tokenParams =
        UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().get("sessionToken");
    return (tokenParams != null && !tokenParams.isEmpty()) ? tokenParams.getFirst() : null;
  }

  private boolean verifyTenantId(String token, long tenantId) {
    try {
      var claims = jwtDecoder.decode(token).getClaims();
      var sessionToken = mapper.convertValue(claims.get("dat"), SessionToken.class);
      return sessionToken.getAccountId() == tenantId;
    } catch (Exception e) {
      log.error("Token verification failed for tenant ID", e);
      return false;
    }
  }

  private void sendNotification(WebSocketSession session, TextMessage message, long tenantId) {
    try {
      MDC.put("tenant_id", String.valueOf(tenantId));
      MDC.put("session_id", session.getId());
      log.debug("Sending notification message to tenant user");

      session.sendMessage(message);
    } catch (Exception e) {
      log.error("Failed to send message to session", e);
    }
  }

  private void setMdcForSession(WebSocketSession session) {
    MDC.put("session_id", session.getId());
    MDC.put("session_uri", String.valueOf(session.getUri()));
  }
}
