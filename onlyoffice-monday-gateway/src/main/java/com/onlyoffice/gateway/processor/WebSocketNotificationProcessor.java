/**
 * (c) Copyright Ascensio System SIA 2026
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
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
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
    implements MessageListener, NotificationProcessor, DisposableBean {
  @Value("${websocket.notification.max-connections:1000}")
  private int maxConnections;

  private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
  private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
  private final AtomicInteger activeConnectionsCount = new AtomicInteger(0);

  private final JwtDecoder jwtDecoder;
  private final ObjectMapper mapper;

  public void afterConnectionEstablished(@NotNull WebSocketSession session) {
    try {
      if (activeConnectionsCount.get() >= maxConnections) {
        log.warn(
            "Maximum WebSocket connections ({}) reached. Rejecting new connection.",
            maxConnections);
        session.close(CloseStatus.SERVICE_OVERLOAD);
        return;
      }

      var sessionToken = getSessionToken(session);
      if (sessionToken == null) {
        log.warn("WebSocket connection attempt without valid session token. Rejecting connection.");
        session.close(CloseStatus.POLICY_VIOLATION);
        return;
      }

      var existingSession = sessionMap.get(sessionToken);
      if (existingSession != null && existingSession.isOpen()) {
        log.warn("Duplicate session token detected. Closing the connection.");
        try {
          session.close(CloseStatus.SESSION_NOT_RELIABLE);
          return;
        } catch (Exception ex) {
          log.error("Error closing existing session: {}", ex.getMessage());
          return;
        }
      }

      sessionMap.put(sessionToken, session);
      sessions.add(session);
      activeConnectionsCount.incrementAndGet();

      log.debug(
          "WebSocket connection established. Active connections: {}", activeConnectionsCount.get());
    } catch (Exception e) {
      log.error("Error establishing WebSocket connection: {}", e.getMessage());
      try {
        session.close(CloseStatus.SERVER_ERROR);
      } catch (Exception ex) {
        log.error("Error closing WebSocket session: {}", ex.getMessage());
      }
    }
  }

  public void afterConnectionClosed(
      @NotNull WebSocketSession session, @NotNull CloseStatus status) {
    try {
      log.debug("Removing websocket session from current instance registry");

      String cleanupSession = null;
      for (var entry : sessionMap.entrySet()) {
        if (entry.getValue().getId().equals(session.getId())) {
          cleanupSession = entry.getKey();
          break;
        }
      }

      if (cleanupSession != null) sessionMap.remove(cleanupSession);

      var removed = sessions.remove(session);
      if (removed) activeConnectionsCount.decrementAndGet();

      log.debug(
          "WebSocket connection closed. Active connections: {}", activeConnectionsCount.get());
    } finally {
      MDC.clear();
    }
  }

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
      log.error("Could not notify a session: {}", e.getMessage());
    } finally {
      MDC.clear();
    }
  }

  private int extractTenantId(String message) {
    try {
      return mapper.readTree(message).get("tenant_id").asInt();
    } catch (Exception e) {
      log.error("Failed to extract tenant id from message: {}", e.getMessage());
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
      log.error("Token verification failed for tenant {}: {}", tenantId, e.getMessage());
      return false;
    }
  }

  private void sendNotification(WebSocketSession session, TextMessage message, long tenantId) {
    try {
      log.debug("Sending notification message to tenant user");

      session.sendMessage(message);
    } catch (Exception e) {
      log.error("Failed to send message to session: {}", e.getMessage());
    }
  }

  public void destroy() throws Exception {
    log.info("Shutting down WebSocketNotificationProcessor, closing {} sessions", sessions.size());
    closeAllSessions();
  }

  private void closeAllSessions() {
    var shutdownStatus = new CloseStatus(CloseStatus.GOING_AWAY.getCode(), "Server shutting down");

    for (var session : sessions) {
      try {
        if (session.isOpen()) session.close(shutdownStatus);
      } catch (IOException e) {
        log.error("Error closing WebSocket session during shutdown: {}", e.getMessage());
      }
    }

    sessions.clear();
    sessionMap.clear();
    activeConnectionsCount.set(0);
  }
}
