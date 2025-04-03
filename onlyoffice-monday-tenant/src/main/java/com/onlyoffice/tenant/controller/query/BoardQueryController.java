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
package com.onlyoffice.tenant.controller.query;

import com.onlyoffice.common.tenant.transfer.request.query.FindEntity;
import com.onlyoffice.common.tenant.transfer.response.BoardInformation;
import com.onlyoffice.tenant.service.query.BoardQueryService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Add Handlers with X-Timeout
@Validated
@RestController
@RequestMapping(
    value = "/tenants/boards",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class BoardQueryController {
  private final BoardQueryService queryService;

  @GetMapping("/{boardId}")
  @RateLimiter(name = "findBoard")
  public ResponseEntity<BoardInformation> findBoard(@PathVariable @Positive long boardId) {
    return ResponseEntity.ok(queryService.find(FindEntity.builder().id(boardId).build()));
  }
}
