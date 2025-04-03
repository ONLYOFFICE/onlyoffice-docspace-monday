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
package com.onlyoffice.gateway.controller.view;

import com.onlyoffice.gateway.client.TenantServiceClient;
import com.onlyoffice.gateway.configuration.i18n.MessageSourceService;
import com.onlyoffice.gateway.controller.view.model.ErrorPageModel;
import com.onlyoffice.gateway.controller.view.model.LoginModel;
import com.onlyoffice.gateway.controller.view.model.PageRendererWrapper;
import com.onlyoffice.gateway.controller.view.model.settings.*;
import com.onlyoffice.gateway.security.MondayAuthenticationPrincipal;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping(
    value = "/views/settings",
    produces = {MediaType.TEXT_HTML_VALUE})
@RequiredArgsConstructor
public class SettingsViewController {
  @Value("${server.origin}")
  private String selfOrigin;

  private final MessageSourceService messageService;
  private final TenantServiceClient tenantService;

  @GetMapping
  public ModelAndView renderSettings(@AuthenticationPrincipal MondayAuthenticationPrincipal user) {
    log.debug("Rendering settings page");
    return getSettingsView(user, false);
  }

  @Secured("ROLE_ADMIN")
  @GetMapping("/change")
  public ModelAndView renderChange(@AuthenticationPrincipal MondayAuthenticationPrincipal user) {
    log.debug("Rendering change page");
    return renderAdminConfigureView(user.getSlug(), true);
  }

  @GetMapping("/refresh")
  public ModelAndView refreshSettings(@AuthenticationPrincipal MondayAuthenticationPrincipal user) {
    log.debug("Refreshing settings page");
    return getSettingsView(user, true);
  }

  private ModelAndView getSettingsView(MondayAuthenticationPrincipal user, boolean partial) {
    var tenantResponse = tenantService.findTenant(user.getAccountId());
    var tenantExists =
        tenantResponse.getStatusCode().is2xxSuccessful() && tenantResponse.getBody() != null;

    if (tenantResponse.getStatusCode().is5xxServerError())
      return renderErrorView(
          messageService.getMessage("pages.errors.unavailable.header"),
          messageService.getMessage("pages.errors.unavailable.subtext"),
          TemplateLocation.SERVER_ERROR.getPath(),
          partial);

    if (!tenantExists) {
      return user.isAdmin()
          ? renderAdminConfigureView(user.getSlug(), partial)
          : renderErrorView(
              messageService.getMessage("pages.errors.configuration.header"),
              messageService.getMessage("pages.errors.configuration.subtext"),
              TemplateLocation.NOT_CONFIGURED_ERROR.getPath(),
              partial);
    }

    var docSpaceUrl = tenantResponse.getBody().getDocSpaceUrl();
    return user.isAdmin()
        ? renderLoginView(
            TemplateLocation.ADMIN_LOGIN,
            buildAdminLoginModel(docSpaceUrl, user.getSlug()),
            partial)
        : renderLoginView(
            TemplateLocation.USER_LOGIN, buildUserLoginModel(docSpaceUrl, user.getSlug()), partial);
  }

  private ModelAndView renderLoginView(TemplateLocation location, Object model, boolean partial) {
    return partial
        ? renderPartialView(location.getPath(), model)
        : renderFullView(location.getPath(), model);
  }

  private ModelAndView renderAdminConfigureView(String slug, boolean partial) {
    var model = buildAdminConfigureModel(slug);
    return partial
        ? renderPartialView(TemplateLocation.ADMIN_CONFIGURE.getPath(), model)
        : renderFullView(TemplateLocation.ADMIN_CONFIGURE.getPath(), model);
  }

  private ModelAndView renderErrorView(
      String header, String subtext, String path, boolean partial) {
    var model =
        ErrorPageModel.builder()
            .refresh("/views/settings/refresh")
            .error(ErrorPageModel.ErrorText.builder().header(header).subtext(subtext).build())
            .build();
    return partial ? renderPartialView(path, model) : renderFullView(path, model);
  }

  private SettingsAdminConfigureModel buildAdminConfigureModel(String slug) {
    return SettingsAdminConfigureModel.builder()
        .login(buildSettingsLoginModel(Strings.EMPTY, slug))
        .information(buildSettingsConfigureInformationModel(slug))
        .settingsForm(buildSettingsLoginFormModel())
        .build();
  }

  private SettingsAdminLoginModel buildAdminLoginModel(String url, String slug) {
    return SettingsAdminLoginModel.builder()
        .settingsForm(buildSettingsLoginFormModel())
        .login(buildSettingsLoginModel(url, getDocSpaceAddress(URI.create(url))))
        .information(buildSettingsConfigureInformationModel(slug))
        .modal(buildSettingsModalModel())
        .build();
  }

  private SettingsUserLoginModel buildUserLoginModel(String url, String slug) {
    return SettingsUserLoginModel.builder()
        .settingsForm(buildSettingsLoginFormModel())
        .login(buildSettingsLoginModel(url, getDocSpaceAddress(URI.create(url))))
        .information(buildSettingsConfigureInformationModel(slug))
        .modal(buildSettingsModalModel())
        .build();
  }

  private SettingsLoginFormModel buildSettingsLoginFormModel() {
    return SettingsLoginFormModel.builder()
        .loginText(messageService.getMessage("pages.settings.configure.field.login"))
        .changeText(messageService.getMessage("pages.settings.configure.field.change"))
        .disclaimer(messageService.getMessage("pages.settings.modal.disclaimer"))
        .fields(
            SettingsLoginFormModel.SettingsLoginFormFields.builder()
                .docSpaceLabel(
                    messageService.getMessage("pages.settings.configure.field.docSpaceLabel"))
                .docSpacePlaceholder(
                    messageService.getMessage("pages.settings.configure.field.docSpacePlaceholder"))
                .emailLabel(messageService.getMessage("pages.settings.configure.field.emailLabel"))
                .emailPlaceholder(
                    messageService.getMessage("pages.settings.configure.field.emailPlaceholder"))
                .passwordLabel(
                    messageService.getMessage("pages.settings.configure.field.passwordLabel"))
                .passwordPlaceholder(
                    messageService.getMessage("pages.settings.configure.field.passwordPlaceholder"))
                .build())
        .build();
  }

  private LoginModel buildSettingsLoginModel(String url, String docSpaceAddress) {
    return LoginModel.builder()
        .url(url)
        .accessText(messageService.getMessage("pages.settings.configure.login.accessText"))
        .addressText(docSpaceAddress)
        .error(messageService.getMessage("pages.settings.configure.login.error"))
        .success(messageService.getMessage("pages.settings.configure.login.success"))
        .cspError(messageService.getMessage("pages.settings.configure.login.cspError"))
        .sizeHeaderError(
            messageService.getMessage("pages.settings.configure.login.sizeHeaderError"))
        .sizeHeaderText(messageService.getMessage("pages.settings.configure.login.sizeHeaderText"))
        .build();
  }

  private SettingsConfigureInformationModel buildSettingsConfigureInformationModel(String slug) {
    return SettingsConfigureInformationModel.builder()
        .csp(messageService.getMessage("pages.settings.configure.information.csp"))
        .credentialsFirst(
            messageService.getMessage("pages.settings.configure.information.credentialsFirst"))
        .credentialsPath(
            messageService.getMessage("pages.settings.configure.information.credentialsPath"))
        .credentialsSecond(
            messageService.getMessage("pages.settings.configure.information.credentialsSecond"))
        .monday(messageService.getMessage("pages.settings.configure.information.monday"))
        .mondayAddress(String.format("https://%s.monday.com", slug))
        .app(messageService.getMessage("pages.settings.configure.information.app"))
        .appAddress(selfOrigin)
        .build();
  }

  private SettingsModalModel buildSettingsModalModel() {
    return SettingsModalModel.builder()
        .confirmActionText(messageService.getMessage("pages.settings.modal.confirmActionText"))
        .agreementText(messageService.getMessage("pages.settings.modal.agreementText"))
        .changeText(messageService.getMessage("pages.settings.modal.changeText"))
        .cancelText(messageService.getMessage("pages.settings.modal.cancelText"))
        .changeError(messageService.getMessage("pages.settings.modal.changeError"))
        .build();
  }

  private String getDocSpaceAddress(URI uri) {
    var parts = uri.getHost().split("\\.");
    var subdomain =
        parts.length > 2
            ? String.join(".", java.util.Arrays.copyOfRange(parts, 0, parts.length - 2))
            : "";
    var domain = parts.length > 1 ? parts[parts.length - 2] : "";
    var zone = parts.length > 0 ? parts[parts.length - 1] : "";
    return String.format("%s.%s.%s", subdomain, domain, zone);
  }

  private <T> ModelAndView renderFullView(String location, T data) {
    return new ModelAndView(
        "pages/root", "page", PageRendererWrapper.builder().location(location).data(data).build());
  }

  private <T> ModelAndView renderPartialView(String location, T data) {
    return new ModelAndView(
        location, "page", PageRendererWrapper.builder().location(location).data(data).build());
  }
}
