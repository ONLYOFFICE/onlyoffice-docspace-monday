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
package com.onlyoffice.gateway.security;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class MondayAuthenticationPrincipalDeserializer
    extends JsonDeserializer<MondayAuthenticationPrincipal> {
  public MondayAuthenticationPrincipal deserialize(
      JsonParser parser, DeserializationContext deserializationContext) throws IOException {
    JsonNode node = parser.getCodec().readTree(parser);
    return MondayAuthenticationPrincipal.builder()
        .userId(node.get("user_id").asLong())
        .accountId(node.get("account_id").asLong())
        .slug(node.get("slug").asText())
        .isAdmin(node.get("is_admin").asBoolean())
        .isViewOnly(node.get("is_view_only").asBoolean())
        .isGuest(node.get("is_guest").asBoolean())
        .build();
  }
}
