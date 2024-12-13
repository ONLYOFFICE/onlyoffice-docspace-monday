package com.onlyoffice.tenant.client;

import com.onlyoffice.common.user.transfer.response.DocSpaceUsers;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

// TODO: Distributed caching in v2?
@FeignClient(
    name = "${spring.cloud.feign.client.onlyoffice-user-name}",
    configuration = UserServiceClientConfiguration.class,
    fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {
  @GetMapping("/users/{tenantId}")
  @Retry(name = "userServiceRetry")
  @CircuitBreaker(name = "userServiceCircuitBreaker")
  ResponseEntity<DocSpaceUsers> findDocSpaceUsers(
      @PathVariable long tenantId, @RequestParam("id") Set<String> ids);

  @GetMapping("/users/{tenantId}")
  @Retry(name = "userServiceRetry")
  @CircuitBreaker(name = "userServiceCircuitBreaker")
  ResponseEntity<DocSpaceUsers> findDocSpaceUsers(
      @PathVariable long tenantId,
      @RequestParam("id") Set<String> ids,
      @RequestHeader("X-Timeout") int timeout);
}
