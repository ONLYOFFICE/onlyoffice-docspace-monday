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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserRepositoryImpl implements TimeoutUserRepository {
  @PersistenceContext private EntityManager entityManager;

  public Optional<User> findByIdWithTimeout(UserId id, int timeoutInMillis) {
    return entityManager
        .createQuery("SELECT u FROM User u WHERE u.id = :id", User.class)
        .setParameter("id", id)
        .setHint("jakarta.persistence.query.timeout", timeoutInMillis)
        .getResultList()
        .stream()
        .findFirst();
  }

  public Set<User> findAllByIdsWithTimeout(Set<UserId> ids, int timeoutInMillis) {
    return new HashSet<>(
        entityManager
            .createQuery("SELECT u FROM User u WHERE u.id IN :ids", User.class)
            .setParameter("ids", ids)
            .setHint("jakarta.persistence.query.timeout", timeoutInMillis)
            .getResultList());
  }
}
