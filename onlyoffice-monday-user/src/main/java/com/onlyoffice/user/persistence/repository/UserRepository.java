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
package com.onlyoffice.user.persistence.repository;

import com.onlyoffice.user.persistence.entity.User;
import com.onlyoffice.user.persistence.entity.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UserId>, TimeoutUserRepository {
  @Modifying
  @Query("DELETE FROM User u WHERE u.tenantId = :tenantId AND u.updatedAt <= :timestamp")
  void deleteAllByTenantIdAndUpdatedAtLessThanEqual(
      @Param("tenantId") long tenantId, @Param("timestamp") long timestamp);
}
