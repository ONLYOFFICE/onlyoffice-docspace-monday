package com.onlyoffice.gateway.controller.view.model.settings;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsConfigureInformationModel {
  @Builder.Default private String csp = "Check the CSP settings";

  @Builder.Default
  private String credentials =
      "Before connecting the app, please go to the DocSpace Settings - Developer tools - JavaScript SDK and add the following credentials to the allow list:";

  @Builder.Default private String monday = "Monday portal address:";
  private String mondayAddress;
  @Builder.Default private String app = "ONLYOFFICE DocSpace app:";
  private String appAddress;
}
