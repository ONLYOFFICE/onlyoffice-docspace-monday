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
package com.onlyoffice.user.service.query;

import com.onlyoffice.common.user.transfer.request.query.FindDocSpaceUsers;
import com.onlyoffice.common.user.transfer.request.query.FindUser;
import com.onlyoffice.common.user.transfer.response.DocSpaceUsers;
import com.onlyoffice.common.user.transfer.response.UserCredentials;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

public interface UserQueryService {
  UserCredentials findUser(@Valid FindUser query);

  UserCredentials findUser(@Valid FindUser query, @Positive int timeout);

  DocSpaceUsers findDocSpaceUsers(@Valid FindDocSpaceUsers query);

  DocSpaceUsers findDocSpaceUsers(@Valid FindDocSpaceUsers query, @Positive int timeout);
}
