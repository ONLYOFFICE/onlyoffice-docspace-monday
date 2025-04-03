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
package com.onlyoffice.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@EnableWebSocket
@EnableFeignClients
@EnableDiscoveryClient
@EnableAspectJAutoProxy
@EnableConfigurationProperties
@EnableMethodSecurity(securedEnabled = true)
@SpringBootApplication(scanBasePackages = {"com.onlyoffice.gateway", "com.onlyoffice.common"})
public class GatewayServerApplication {
  public static void main(String[] args) {
    SpringApplication.run(GatewayServerApplication.class, args);
  }
}
