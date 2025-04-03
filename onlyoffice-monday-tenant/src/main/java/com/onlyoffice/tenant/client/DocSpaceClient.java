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
package com.onlyoffice.tenant.client;

import com.onlyoffice.common.docspace.transfer.request.command.AuthenticateUser;
import com.onlyoffice.common.docspace.transfer.request.command.ChangeRoomAccess;
import com.onlyoffice.common.docspace.transfer.response.GenericResponse;
import com.onlyoffice.common.docspace.transfer.response.MembersAccess;
import com.onlyoffice.common.docspace.transfer.response.RoomLink;
import com.onlyoffice.common.docspace.transfer.response.UserToken;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;

// TODO: Distributed caching in v2?
@FeignClient(name = "docSpaceClient", fallbackFactory = DocSpaceClientFallbackFactory.class)
public interface DocSpaceClient {
  @Cacheable(value = "userTokens", key = "#command.userName")
  @RequestLine("POST /api/2.0/authentication")
  @Headers("Content-Type: application/json")
  @Retry(name = "docSpaceClientRetry")
  @CircuitBreaker(name = "docSpaceClientCircuitBreaker")
  GenericResponse<UserToken> generateToken(URI baseUri, AuthenticateUser command);

  @Cacheable(value = "roomLinks", key = "#roomId")
  @RequestLine("GET /api/2.0/files/rooms/{roomId}/links?type=1")
  @Headers({"Authorization: {token}", "Content-Type: application/json"})
  @Retry(name = "docSpaceClientRetry")
  @CircuitBreaker(name = "docSpaceClientCircuitBreaker")
  GenericResponse<List<RoomLink>> generateSharedKey(
      URI baseUri, @Param("roomId") long roomId, @Param("token") String token);

  @RequestLine("PUT /api/2.0/files/rooms/{roomId}/share")
  @Headers({"Authorization: {token}", "Content-Type: application/json"})
  @Retry(name = "docSpaceClientRetry")
  @CircuitBreaker(name = "docSpaceClientCircuitBreaker")
  GenericResponse<MembersAccess> changeRoomAccess(
      URI baseUri,
      @Param("roomId") long roomId,
      @Param("token") String token,
      ChangeRoomAccess command);
}
