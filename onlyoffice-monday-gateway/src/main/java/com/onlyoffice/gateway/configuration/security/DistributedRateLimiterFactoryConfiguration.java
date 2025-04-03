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

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class DistributedRateLimiterFactoryConfiguration {
  private final RedisConfigurationProperties redisConfigurationProperties;
  private final DistributedRateLimiterConfiguration configuration;

  @Bean
  public RedisClient redisClient(LettuceConnectionFactory lettuceConnectionFactory) {
    return RedisClient.create(
        RedisURI.builder()
            .withHost(redisConfigurationProperties.getHost())
            .withPort(redisConfigurationProperties.getPort())
            .withDatabase(redisConfigurationProperties.getDatabase())
            .withSsl(redisConfigurationProperties.getSsl().isEnabled())
            .withAuthentication(
                redisConfigurationProperties.getUsername(),
                redisConfigurationProperties.getPassword().toCharArray())
            .build());
  }

  @Bean
  public LettuceBasedProxyManager lettuceBasedProxyManager(RedisClient redisClient) {
    return LettuceBasedProxyManager.builderFor(redisClient)
        .withExpirationStrategy(
            ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                Duration.ofMinutes(1)))
        .build();
  }

  public Function<HttpMethod, Supplier<BucketConfiguration>> bucketConfigurationFactory() {
    var rateLimitProperties = configuration.getRateLimits();
    return (HttpMethod method) -> {
      var config =
          rateLimitProperties.stream()
              .filter(props -> props.getMethod().equalsIgnoreCase(method.name()))
              .findFirst()
              .orElse(
                  DistributedRateLimiterConfiguration.RateLimitProperties.builder()
                      .capacity(5)
                      .method(method.name())
                      .refill(
                          DistributedRateLimiterConfiguration.RateLimitProperties.Refill.builder()
                              .tokens(5)
                              .period(1)
                              .timeUnit(ChronoUnit.SECONDS)
                              .build())
                      .build());

      return () ->
          BucketConfiguration.builder()
              .addLimit(
                  Bandwidth.classic(
                      config.getCapacity(),
                      Refill.greedy(
                          config.getRefill().getTokens(),
                          Duration.of(
                              config.getRefill().getPeriod(), config.getRefill().getTimeUnit()))))
              .build();
    };
  }
}
