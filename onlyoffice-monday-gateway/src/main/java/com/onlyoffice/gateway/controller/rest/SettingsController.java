package com.onlyoffice.gateway.controller.rest;

import com.onlyoffice.common.client.notification.factory.NotificationPublisherFactory;
import com.onlyoffice.common.client.notification.transfer.event.NotificationEvent;
import com.onlyoffice.common.client.notification.transfer.event.TenantChanged;
import com.onlyoffice.common.service.encryption.EncryptionService;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterTenant;
import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.gateway.client.TenantServiceClient;
import com.onlyoffice.gateway.client.UserServiceClient;
import com.onlyoffice.gateway.security.MondayAuthenticationPrincipal;
import com.onlyoffice.gateway.transport.rest.request.LoginUserCommand;
import com.onlyoffice.gateway.transport.rest.request.SaveSettingsCommand;
import jakarta.validation.Valid;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: MDC Aspect
@Slf4j
@RestController
@RequestMapping(value = "/api/1.0/settings")
public class SettingsController {
  private final TenantServiceClient tenantService;
  private final UserServiceClient userService;
  private final Consumer<NotificationEvent> messagePublisher;
  private final EncryptionService encryptionService;

  public SettingsController(
      TenantServiceClient tenantService,
      UserServiceClient userService,
      NotificationPublisherFactory factory,
      EncryptionService encryptionService) {
    this.tenantService = tenantService;
    this.userService = userService;
    this.messagePublisher = factory.getPublisher("notifications");
    this.encryptionService = encryptionService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @AuthenticationPrincipal MondayAuthenticationPrincipal user,
      @RequestBody @Valid LoginUserCommand body) {
    try {
      MDC.put("tenant_id", String.valueOf(user.getAccountId()));
      MDC.put("user_id", String.valueOf(user.getUserId()));
      log.info("User attempts to persist login information");

      var response =
          userService.registerUser(
              RegisterUser.builder()
                  .mondayId(user.getUserId())
                  .tenantId(user.getAccountId())
                  .docSpaceId(body.getDocSpaceUserId())
                  .email(body.getDocSpaceEmail())
                  .hash(encryptionService.encrypt(body.getDocSpaceHash()))
                  .build());

      if (!response.getStatusCode().is2xxSuccessful())
        return ResponseEntity.status(response.getStatusCode()).header("HX-Refresh", "true").build();

      return ResponseEntity.status(HttpStatus.OK).header("HX-Refresh", "true").build();
    } finally {
      MDC.clear();
    }
  }

  @PostMapping
  @Secured("ROLE_ADMIN")
  public ResponseEntity<?> saveSettings(
      @AuthenticationPrincipal MondayAuthenticationPrincipal user,
      @RequestBody SaveSettingsCommand body) {
    try {
      MDC.put("tenant_id", String.valueOf(user.getAccountId()));
      MDC.put("user_id", String.valueOf(user.getUserId()));
      log.info("User attempts to save tenant DocSpace credentials");

      var response =
          tenantService.createTenant(
              RegisterTenant.builder()
                  .id(user.getAccountId())
                  .mondayUserId(user.getUserId())
                  .url(body.getDocSpaceUrl())
                  .docSpaceUserId(body.getDocSpaceUserId())
                  .adminLogin(body.getDocSpaceEmail())
                  .adminHash(encryptionService.encrypt(body.getDocSpaceHash()))
                  .build());

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.error("Could not save tenant DocSpace credentials");
        return ResponseEntity.status(response.getStatusCode()).build();
      }

      messagePublisher.accept(TenantChanged.builder().tenantId(user.getAccountId()).build());
      log.debug("Tenant changed notification has been sent");

      return ResponseEntity.ok().header("HX-Refresh", "true").build();
    } finally {
      MDC.clear();
    }
  }
}
