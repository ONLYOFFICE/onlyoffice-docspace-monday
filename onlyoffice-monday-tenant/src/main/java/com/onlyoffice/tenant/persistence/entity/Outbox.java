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
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

// TODO: Replace outbox with CDC in v2?
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "tenants", name = "monday_outbox")
public class Outbox {
  @Id @UuidGenerator private String id;

  @JdbcTypeCode(SqlTypes.JSON)
  private String payload;

  @Enumerated(EnumType.STRING)
  private OutboxType type;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Long createdAt;

  @PrePersist
  protected void prePersist() {
    createdAt = Instant.now().toEpochMilli();
  }
}
