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
package com.onlyoffice.gateway.configuration.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSourceLocaleService implements MessageSourceService {
  private final MessageSource messageSource;

  @Lookup
  public InternalizationLocaleContext localeContext() {
    return null;
  }

  public String getMessage(String code, String... args) {
    return messageSource.getMessage(code, args, localeContext().getCurrent());
  }
}
