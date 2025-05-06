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
package com.onlyoffice.gateway.transport.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.onlyoffice.common.validation.Url;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveSettingsCommand {
  @JsonProperty("docspace_url")
  @NotBlank(message = "DocSpace url is required")
  @Url(message = "DocSpace url must be a valid and safe URL")
  private String docSpaceUrl;

  @JsonProperty("docspace_user_id")
  @NotBlank(message = "DocSpace user id is required")
  private String docSpaceUserId;

  @JsonProperty("docspace_email")
  @NotBlank(message = "DocSpace user email is required")
  @Email(message = "Invalid email format")
  private String docSpaceEmail;

  @JsonProperty("docspace_hash")
  @NotBlank(message = "DocSpace user hash is required")
  private String docSpaceHash;
}
