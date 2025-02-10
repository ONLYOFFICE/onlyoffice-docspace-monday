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
package com.onlyoffice.gateway.client;

import com.onlyoffice.gateway.exception.service.ServiceBadRequestException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class TenantServiceClientErrorDecoder implements ErrorDecoder {
  private final ErrorDecoder defaultErrorDecoder = new Default();

  public Exception decode(String methodKey, Response response) {
    if (response.status() == 400)
      return new ServiceBadRequestException("Could not perform operation due to BadRequest status");
    return defaultErrorDecoder.decode(methodKey, response);
  }
}
