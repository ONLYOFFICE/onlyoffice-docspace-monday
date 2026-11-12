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
package com.onlyoffice.gateway.smoke;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.onlyoffice.common.client.notification.factory.NotificationPublisherFactory;
import com.onlyoffice.common.client.notification.transfer.event.NotificationEvent;
import com.onlyoffice.common.client.notification.transfer.event.RoomCreated;
import com.onlyoffice.common.client.notification.transfer.event.TenantChanged;
import com.onlyoffice.common.docspace.transfer.response.CspInfo;
import com.onlyoffice.common.docspace.transfer.response.GenericResponse;
import com.onlyoffice.common.service.encryption.AesEncryptionService;
import com.onlyoffice.common.service.encryption.EncryptionService;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterRoom;
import com.onlyoffice.common.tenant.transfer.request.command.RegisterTenant;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveRoom;
import com.onlyoffice.common.tenant.transfer.request.command.RemoveTenant;
import com.onlyoffice.common.tenant.transfer.response.TenantCredentials;
import com.onlyoffice.gateway.client.DocSpaceClient;
import com.onlyoffice.gateway.client.TenantServiceClient;
import com.onlyoffice.gateway.client.UserServiceClient;
import com.onlyoffice.gateway.controller.rest.RoomController;
import com.onlyoffice.gateway.controller.rest.SettingsController;
import com.onlyoffice.gateway.controller.rest.WebhookController;
import com.onlyoffice.gateway.security.MondayAuthenticationFilter;
import com.onlyoffice.gateway.security.MondayAuthenticationPrincipal;
import com.onlyoffice.gateway.security.MondayAuthenticationProvider;
import com.onlyoffice.gateway.security.MondayWebhookAuthenticationFilter;
import com.onlyoffice.gateway.transport.rest.request.CreateRoomCommand;
import com.onlyoffice.gateway.transport.rest.request.SaveSettingsCommand;
import com.onlyoffice.gateway.transport.rest.request.WebhookCommandTenantPayload;
import com.onlyoffice.gateway.transport.rest.request.WebhookWrapperCommand;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

class GatewaySmokeTest {
  private static final String CLIENT_SECRET = "0123456789abcdef0123456789abcdef";
  private static final String APP_ORIGIN = "https://monday.example.test";
  private static final long ACCOUNT_ID = 100L;
  private static final long USER_ID = 200L;

  private final JwtDecoder decoder =
      NimbusJwtDecoder.withSecretKey(new SecretKeySpec(CLIENT_SECRET.getBytes(), "HmacSHA256"))
          .build();
  private final ProviderManager authenticationManager =
      new ProviderManager(new MondayAuthenticationProvider());

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  private static String mondayToken(String secret, boolean admin) throws Exception {
    var claims =
        new JWTClaimsSet.Builder()
            .claim(
                "dat",
                Map.of(
                    "user_id",
                    USER_ID,
                    "account_id",
                    ACCOUNT_ID,
                    "slug",
                    "acme",
                    "is_admin",
                    admin,
                    "is_view_only",
                    false,
                    "is_guest",
                    false))
            .expirationTime(Date.from(Instant.now().plusSeconds(300)))
            .build();
    var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    jwt.sign(new MACSigner(secret.getBytes()));
    return jwt.serialize();
  }

  private static MondayAuthenticationPrincipal principal(boolean admin) {
    return MondayAuthenticationPrincipal.builder()
        .userId(USER_ID)
        .accountId(ACCOUNT_ID)
        .slug("acme")
        .isAdmin(admin)
        .build();
  }

  @Nested
  class MondaySessionAuthentication {
    private MondayAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
      filter = new MondayAuthenticationFilter(decoder, authenticationManager);
    }

    @Test
    void rejectsRequestWithoutSessionToken() throws Exception {
      var request = new MockHttpServletRequest("GET", "/views");
      var response = new MockHttpServletResponse();
      var chain = new MockFilterChain();

      filter.doFilter(request, response, chain);

      assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
      assertThat(chain.getRequest()).isNull();
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() throws Exception {
      var request = new MockHttpServletRequest("GET", "/views");
      request.setParameter("sessionToken", mondayToken("ffffffffffffffffffffffffffffffff", true));
      var response = new MockHttpServletResponse();
      var chain = new MockFilterChain();

      filter.doFilter(request, response, chain);

      assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      assertThat(chain.getRequest()).isNull();
    }

    @Test
    void authenticatesAdminFromQueryToken() throws Exception {
      var request = new MockHttpServletRequest("GET", "/views");
      request.setParameter("sessionToken", mondayToken(CLIENT_SECRET, true));
      var chain = new MockFilterChain();

      filter.doFilter(request, new MockHttpServletResponse(), chain);

      assertThat(chain.getRequest()).isNotNull();
      var auth = SecurityContextHolder.getContext().getAuthentication();
      assertThat(auth.isAuthenticated()).isTrue();
      assertThat(auth.getAuthorities())
          .extracting(GrantedAuthority::getAuthority)
          .containsExactly("ROLE_ADMIN");
      var user = (MondayAuthenticationPrincipal) auth.getPrincipal();
      assertThat(user.getAccountId()).isEqualTo(ACCOUNT_ID);
      assertThat(user.getSlug()).isEqualTo("acme");
    }

    @Test
    void authenticatesHtmxRequestFromCurrentUrlHeader() throws Exception {
      var request = new MockHttpServletRequest("GET", "/views/refresh");
      request.addHeader(
          "HX-Current-URL",
          APP_ORIGIN + "/views?sessionToken=" + mondayToken(CLIENT_SECRET, false));
      var chain = new MockFilterChain();

      filter.doFilter(request, new MockHttpServletResponse(), chain);

      assertThat(chain.getRequest()).isNotNull();
      assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
          .extracting(GrantedAuthority::getAuthority)
          .containsExactly("ROLE_MEMBER");
    }

    @Test
    void skipsStaticAssetsAndWebhook() throws Exception {
      for (var path :
          List.of("/main.css", "/webSocket.js", "/api/1.0/webhook", "/actuator/health")) {
        var request = new MockHttpServletRequest("GET", path);
        request.setRequestURI(path);
        var chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).as(path).isNotNull();
      }
    }
  }

  @Nested
  class UninstallWebhook {
    @Test
    void acceptsSignedWebhookAndRemovesTenant() throws Exception {
      var filter = new MondayWebhookAuthenticationFilter(decoder, authenticationManager);
      var request = new MockHttpServletRequest("POST", "/api/1.0/webhook");
      request.setRequestURI("/api/1.0/webhook");
      request.addHeader("Authorization", mondayToken(CLIENT_SECRET, true));
      var chain = new MockFilterChain();

      filter.doFilter(request, new MockHttpServletResponse(), chain);
      assertThat(chain.getRequest()).isNotNull();

      var tenantService = mock(TenantServiceClient.class);
      var controller = new WebhookController(tenantService);
      var response =
          controller.process(
              WebhookWrapperCommand.<WebhookCommandTenantPayload>builder()
                  .type("uninstall")
                  .data(WebhookCommandTenantPayload.builder().tenantId(ACCOUNT_ID).build())
                  .build());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(tenantService).removeTenant(RemoveTenant.builder().tenantId(ACCOUNT_ID).build());
    }

    @Test
    void rejectsWebhookWithoutAuthorization() throws Exception {
      var filter = new MondayWebhookAuthenticationFilter(decoder, authenticationManager);
      var request = new MockHttpServletRequest("POST", "/api/1.0/webhook");
      request.setRequestURI("/api/1.0/webhook");
      var response = new MockHttpServletResponse();
      var chain = new MockFilterChain();

      filter.doFilter(request, response, chain);

      assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
      assertThat(chain.getRequest()).isNull();
    }
  }

  @Nested
  class BoardRoom {
    private TenantServiceClient tenantService;
    private Consumer<NotificationEvent> publisher;
    private RoomController controller;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
      tenantService = mock(TenantServiceClient.class);
      publisher = mock(Consumer.class);
      var factory = mock(NotificationPublisherFactory.class);
      doReturn(publisher).when(factory).getPublisher("notifications");
      controller = new RoomController(tenantService, factory);
    }

    @Test
    void registersCreatedRoomAndNotifiesClients() {
      when(tenantService.findTenant(ACCOUNT_ID))
          .thenReturn(ResponseEntity.ok(TenantCredentials.builder().id(ACCOUNT_ID).build()));
      doReturn(ResponseEntity.status(HttpStatus.CREATED).build())
          .when(tenantService)
          .createRoom(any());

      var response =
          controller.createRoom(
              principal(true),
              CreateRoomCommand.builder().boardId(10L).roomId(20L).users(Set.of("1")).build());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getFirst("HX-Refresh")).isEqualTo("true");
      verify(tenantService)
          .createRoom(
              RegisterRoom.builder()
                  .tenantId(ACCOUNT_ID)
                  .boardId(10L)
                  .roomId(20L)
                  .mondayUsers(Set.of("1"))
                  .build());
      verify(publisher).accept(RoomCreated.builder().tenantId(ACCOUNT_ID).boardId(10L).build());
    }

    @Test
    void refusesRoomWhenTenantIsNotConfigured() {
      when(tenantService.findTenant(ACCOUNT_ID)).thenReturn(ResponseEntity.notFound().build());

      var response =
          controller.createRoom(
              principal(true),
              CreateRoomCommand.builder().boardId(10L).roomId(20L).users(Set.of()).build());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      verify(tenantService, never()).createRoom(any());
    }

    @Test
    void unlinksRoomFromBoard() {
      doReturn(ResponseEntity.noContent().build()).when(tenantService).removeRoom(any());

      var response = controller.unlinkRoom(principal(true), 10L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(tenantService)
          .removeRoom(RemoveRoom.builder().tenantId(ACCOUNT_ID).boardId(10L).build());
    }
  }

  @Nested
  class AdminSettings {
    private TenantServiceClient tenantService;
    private DocSpaceClient docSpace;
    private Consumer<NotificationEvent> publisher;
    private SettingsController controller;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
      tenantService = mock(TenantServiceClient.class);
      docSpace = mock(DocSpaceClient.class);
      publisher = mock(Consumer.class);
      var factory = mock(NotificationPublisherFactory.class);
      doReturn(publisher).when(factory).getPublisher("notifications");
      var encryption = mock(EncryptionService.class);
      when(encryption.encrypt(any())).thenAnswer(i -> "enc:" + i.getArgument(0));
      controller =
          new SettingsController(tenantService, mock(UserServiceClient.class), factory, encryption);
      ReflectionTestUtils.setField(controller, "docSpaceService", docSpace);
      ReflectionTestUtils.setField(controller, "selfOrigin", APP_ORIGIN);
    }

    private SaveSettingsCommand command() {
      return SaveSettingsCommand.builder()
          .docSpaceUrl("https://docspace.example.test")
          .docSpaceUserId("ds-user")
          .docSpaceEmail("admin@example.test")
          .docSpaceHash("hash")
          .build();
    }

    private void cspDomains(String... domains) {
      var csp = new GenericResponse<CspInfo>();
      csp.setResponse(CspInfo.builder().domains(List.of(domains)).build());
      when(docSpace.checkCSP(any())).thenReturn(csp);
    }

    @Test
    void rejectsDocSpaceWithoutMondayDomainInCsp() {
      cspDomains(APP_ORIGIN);

      var response = controller.saveSettings(principal(true), command());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      verify(tenantService, never()).createTenant(any());
    }

    @Test
    void savesTenantWithEncryptedHashWhenCspIsValid() {
      cspDomains(APP_ORIGIN, "https://acme.monday.com");
      when(tenantService.createTenant(any()))
          .thenReturn(ResponseEntity.ok(TenantCredentials.builder().id(ACCOUNT_ID).build()));

      var response = controller.saveSettings(principal(true), command());

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(tenantService)
          .createTenant(
              argThat(
                  (RegisterTenant t) ->
                      t.getId() == ACCOUNT_ID
                          && t.getAdminHash().equals("enc:hash")
                          && t.getUrl().equals("https://docspace.example.test")));
      verify(publisher).accept(TenantChanged.builder().tenantId(ACCOUNT_ID).build());
    }
  }

  @Nested
  class CredentialEncryption {
    @Test
    void encryptsAndDecryptsStoredHash() {
      var service = new AesEncryptionService();
      ReflectionTestUtils.setField(service, "secret", "test-encryption-secret");

      var cipher = service.encrypt("docspace-hash");

      assertThat(cipher).isNotEqualTo("docspace-hash");
      assertThat(service.decrypt(cipher)).isEqualTo("docspace-hash");
    }
  }
}
