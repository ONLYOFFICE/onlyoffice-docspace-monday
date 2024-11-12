package com.onlyoffice.tenant.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceClientConfiguration {
  @Bean
  public ErrorDecoder errorDecoder() {
    return new UserServiceClientErrorDecoder();
  }
}
