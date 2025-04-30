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
package com.onlyoffice.common.logging.aspect;

import com.onlyoffice.common.logging.UserPrincipal;
import com.onlyoffice.common.logging.UserPrincipalResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A reusable aspect that provides comprehensive logging for REST controllers and security events.
 * This aspect works across all microservices in the application.
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class GenericLoggingAspect {
  private final UserPrincipalResolver userPrincipalResolver;
  private static final String UNKNOWN = "unknown";

  /**
   * Record to store HTTP request information.
   */
  private record RequestInfo(String ipAddress, String method, String uri) {}

  /**
   * Pointcut that matches all REST controllers. Can be customized by each service to match their
   * controller package structure.
   */
  @Pointcut("within(*..*Controller)")
  public void controllerPointcut() {}

  /**
   * Pointcut that matches all security-related authentication methods. Can be customized by each
   * service to match their security package structure.
   */
  @Pointcut("execution(* *..*.*authenticate*(..))")
  public void securityPointcut() {}

  /** Logs all REST controller method invocations. */
  @Around("controllerPointcut()")
  public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
    setupMdc(joinPoint);
    setupUserInfo();

    log.info("Received a new user request");

    logMethodArguments(joinPoint);

    return executeJoinPoint(joinPoint, "User request failed: {}");
  }

  /** Logs all security-related authentication events. */
  @Around("securityPointcut()")
  public Object logSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
    setupMdc(joinPoint);

    log.info("Authenticating a new request");

    return executeJoinPoint(joinPoint, "Authentication failed: {}");
  }

  /**
   * Sets up MDC with request information.
   */
  private void setupMdc(ProceedingJoinPoint joinPoint) {
    var requestInfo = extractRequestInfo();
    var methodName = joinPoint.getSignature().getName();

    MDC.put("ip", requestInfo.ipAddress());
    MDC.put("httpMethod", requestInfo.method());
    MDC.put("method", methodName);
    MDC.put("uri", requestInfo.uri());
  }

  /**
   * Executes the join point and handles exception logging.
   */
  private Object executeJoinPoint(ProceedingJoinPoint joinPoint, String errorMessage) throws Throwable {
    try {
      return joinPoint.proceed();
    } catch (Exception e) {
      log.error(errorMessage, e.getMessage());
      throw e;
    } finally {
      MDC.clear();
    }
  }

  /**
   * Extracts HTTP request information.
   */
  private RequestInfo extractRequestInfo() {
    var ipAddress = UNKNOWN;
    var requestMethod = UNKNOWN;
    var requestURI = UNKNOWN;

    try {
      var requestAttributes = RequestContextHolder.getRequestAttributes();
      if (requestAttributes != null) {
        var request = ((ServletRequestAttributes) requestAttributes).getRequest();
        ipAddress = getClientIpAddress(request);
        requestMethod = request.getMethod();
        requestURI = request.getRequestURI();
      }
    } catch (Exception e) {
      log.warn("Could not extract request info: {}", e.getMessage());
    }

    return new RequestInfo(ipAddress, requestMethod, requestURI);
  }

  /**
   * Logs method argument types for debugging.
   */
  private void logMethodArguments(ProceedingJoinPoint joinPoint) {
    var args = joinPoint.getArgs();
    if (args != null && args.length > 0) {
      var argsTypes =
              Arrays.stream(args)
                      .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                      .reduce((a, b) -> a + ", " + b)
                      .orElse("");
      log.debug("Method argument types: {}", argsTypes);
    }
  }

  /** Sets up user information in MDC from security context. */
  private void setupUserInfo() {
    try {
      var authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null) {
        var userPrincipal =
                userPrincipalResolver.resolveUserPrincipal(authentication);

        userPrincipal.ifPresent(
                user -> {
                  MDC.put("tenantId", String.valueOf(user.getAccountId()));
                  MDC.put("userId", String.valueOf(user.getUserId()));

                  var role = user.getRole();
                  if (role != null) MDC.put("userRole", role);
                });
      }
    } catch (Exception e) {
      log.warn("Could not set user info in MDC", e);
    }
  }

  /** Gets the client IP address accounting for proxy headers. */
  private String getClientIpAddress(HttpServletRequest request) {
    var realAddress = request.getHeader("X-Forwarded-For");
    if (realAddress != null && !realAddress.isEmpty()) return realAddress.split(",")[0];

    realAddress = request.getHeader("X-Real-IP");
    if (realAddress != null && !realAddress.isEmpty()) return realAddress;

    return request.getRemoteAddr();
  }
}
