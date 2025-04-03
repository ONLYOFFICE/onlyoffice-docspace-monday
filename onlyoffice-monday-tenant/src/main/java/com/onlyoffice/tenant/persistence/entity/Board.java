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
package com.onlyoffice.tenant.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    schema = "tenants",
    name = "monday_tenant_registered_boards",
    indexes = {@Index(name = "monday_tenant_boards_idx", columnList = "room_id,tenant_id")})
@EntityListeners(AuditingEntityListener.class)
public class Board {
  // Monday board id
  @Id
  @Column(nullable = false, updatable = false)
  private long id;

  // DocSpace generated id
  @Column(name = "room_id", nullable = false)
  private long roomId;

  @ManyToOne
  @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
  private Tenant tenant;

  // DocSpace public key to view the room
  @Column(name = "access_key")
  private String accessKey;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Long createdAt;

  @Column(name = "updated_at")
  private Long updatedAt;

  @PrePersist
  protected void prePersist() {
    var now = System.currentTimeMillis();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void preUpdate() {
    updatedAt = System.currentTimeMillis();
  }
}
