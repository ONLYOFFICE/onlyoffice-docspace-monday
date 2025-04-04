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
package com.onlyoffice.gateway.configuration.security;

import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("server.bucket4j")
public class DistributedRateLimiterConfiguration {
  private List<RateLimitProperties> rateLimits;

  @Data
  @Builder
  public static class RateLimitProperties {
    private String method;
    private int capacity;
    private Refill refill;

    @Data
    @Builder
    public static class Refill {
      private int tokens;
      private int period;
      private ChronoUnit timeUnit;
    }
  }
}
