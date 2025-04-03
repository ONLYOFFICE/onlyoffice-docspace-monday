/**
 * (c) Copyright Ascensio System SIA 2024
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
package com.onlyoffice.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@Disabled
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"server.port=8080", "server.address=0.0.0.0"})
@ActiveProfiles("performance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerPerformanceTest {
  @Autowired private JdbcTemplate jdbcTemplate;

  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:14-alpine"))
          .withUsername("postgres")
          .withPassword("postgres")
          .withDatabaseName("monday");

  private static final RabbitMQContainer RABBITMQ =
      new RabbitMQContainer(DockerImageName.parse("rabbitmq:latest"));

  private static final GenericContainer<?> REDIS =
      new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    // Start containers if not already started
    if (!POSTGRES.isRunning()) {
      POSTGRES.start();
    }
    if (!RABBITMQ.isRunning()) {
      RABBITMQ.start();
    }
    if (!REDIS.isRunning()) {
      REDIS.start();
    }

    // PostgreSQL
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);

    // Flyway
    registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
    registry.add("spring.flyway.username", POSTGRES::getUsername);
    registry.add("spring.flyway.password", POSTGRES::getPassword);

    // Redis
    registry.add("spring.redis.host", REDIS::getHost);
    registry.add("spring.redis.port", () -> REDIS.getFirstMappedPort().toString());

    // RabbitMQ
    registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
    registry.add("spring.rabbitmq.port", () -> RABBITMQ.getAmqpPort().toString());

    // Eureka
    registry.add("eureka.client.enabled", () -> "false");
    registry.add("eureka.client.fetchRegistry", () -> "false");
  }

  @BeforeAll
  public void setupData() {
    var sql =
        "INSERT INTO users.monday_tenant_users (tenant_id, monday_id, docspace_id, email, hash, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    var batchSize = 1000;
    var batchArgs = new ArrayList<Object[]>(batchSize);

    for (int i = 1; i <= 100_000; i++) {
      var user =
          new Object[] {
            123,
            i,
            UUID.randomUUID().toString(),
            "user" + i + "@example.com",
            "hash",
            Instant.now().toEpochMilli()
          };
      batchArgs.add(user);

      if (batchArgs.size() == batchSize) {
        jdbcTemplate.batchUpdate(sql, batchArgs);
        batchArgs.clear();
      }
    }

    if (!batchArgs.isEmpty()) jdbcTemplate.batchUpdate(sql, batchArgs);
  }

  @Test
  @Order(1)
  public void findUserLoadTest() throws Exception {
    executeK6Test("/k6/find_user_test.js");
  }

  @Test
  @Order(2)
  public void findUserStressTest() throws Exception {
    executeK6Test("/k6/find_user_stress_test.js");
  }

  @Test
  @Order(3)
  public void registerUserLoadTest() throws Exception {
    executeK6Test("/k6/register_user_stress_test.js");
  }

  private void executeK6Test(String scriptPath) throws Exception {
    var processBuilder =
        new ProcessBuilder(
            "docker",
            "run",
            "--rm",
            "--add-host=host.docker.internal:host-gateway",
            "-v",
            System.getProperty("user.dir") + "/src/test/k6:/k6",
            "grafana/k6",
            "run",
            scriptPath);
    processBuilder.redirectErrorStream(true);

    var process = processBuilder.start();
    var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    var line = Strings.EMPTY;
    var output = new StringBuilder();

    while ((line = reader.readLine()) != null) {
      System.out.println(line);
      output.append(line).append("\n");
    }

    var exitCode = process.waitFor();
    assertEquals(0, exitCode, "K6 tests failed with exit code " + exitCode);
  }

  @AfterAll
  public void tearDown() {
    if (REDIS.isRunning()) REDIS.stop();
    if (RABBITMQ.isRunning()) RABBITMQ.stop();
    if (POSTGRES.isRunning()) POSTGRES.stop();
  }
}
