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
package com.onlyoffice.user.processor;

import com.onlyoffice.common.CommandMessage;
import com.onlyoffice.common.user.transfer.request.command.RegisterUser;
import com.onlyoffice.common.user.transfer.request.command.RemoveTenantUsers;
import com.onlyoffice.user.service.command.UserCommandService;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProcessor {
  private final UserCommandService userCommandService;

  @Bean
  public Consumer<CommandMessage<RegisterUser>> registerUserConsumer() {
    return userCommandService::register;
  }

  @Bean
  public Consumer<CommandMessage<RemoveTenantUsers>> removeTenantUsersConsumer() {
    return userCommandService::removeAll;
  }
}
