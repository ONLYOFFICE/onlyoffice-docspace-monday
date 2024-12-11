package com.onlyoffice.user.controller;

import com.onlyoffice.common.user.transfer.request.query.FindDocSpaceUsers;
import com.onlyoffice.common.user.transfer.request.query.FindUser;
import com.onlyoffice.common.user.transfer.response.DocSpaceUsers;
import com.onlyoffice.common.user.transfer.response.UserCredentials;
import com.onlyoffice.user.service.query.UserQueryService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping(
    value = "/users",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class UserQueryController {
  private final UserQueryService queryService;

  @RateLimiter(name = "getUser")
  @GetMapping("/{tenantId}/{mondayId}")
  public ResponseEntity<UserCredentials> findUser(
      @PathVariable("tenantId") @Positive long tenantId,
      @PathVariable("mondayId") @Positive long mondayId,
      @RequestHeader(value = "X-Timeout", defaultValue = "3500") int timeout) {
    return ResponseEntity.ok(
        queryService.findUser(
            FindUser.builder().tenantId(tenantId).mondayId(mondayId).build(), timeout));
  }

  @RateLimiter(name = "getUser")
  @GetMapping("/{tenantId}")
  public ResponseEntity<DocSpaceUsers> findDocSpaceUsers(
      @PathVariable("tenantId") @Positive long tenantId,
      @RequestParam("id") Set<Long> ids,
      @RequestHeader(value = "X-Timeout", defaultValue = "3500") int timeout) {
    return ResponseEntity.ok(
        queryService.findDocSpaceUsers(
            FindDocSpaceUsers.builder().tenantId(tenantId).mondayIds(ids).build(), timeout));
  }
}
