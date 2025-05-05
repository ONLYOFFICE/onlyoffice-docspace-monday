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
package com.onlyoffice.common.logging;

import com.onlyoffice.common.logging.aspect.GenericLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Auto-configuration for the common logging infrastructure. Services can import this configuration
 * to enable standardized logging.
 */
@Configuration
@EnableAspectJAutoProxy
public class GenericLoggingAutoConfiguration {

  /**
   * Provides the request logging aspect if a UserPrincipalResolver is available.
   *
   * @param userPrincipalResolver The resolver to convert authentication objects to UserPrincipals
   * @return The RequestLoggingAspect bean
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(UserPrincipalResolver.class)
  public GenericLoggingAspect requestLoggingAspect(UserPrincipalResolver userPrincipalResolver) {
    return new GenericLoggingAspect(userPrincipalResolver);
  }
}
