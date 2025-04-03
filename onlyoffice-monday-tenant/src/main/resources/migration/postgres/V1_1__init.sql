CREATE SCHEMA IF NOT EXISTS tenants;

CREATE TABLE tenants.monday_outbox (
	id VARCHAR(255) NOT NULL,
	payload JSONB,
	type VARCHAR(255) CHECK (type IN ('REFRESH','INVITE','CREATE_USER_ON_INITIALIZATION','REMOVE_TENANT_USERS')),
	created_at BIGINT NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE tenants.monday_tenant_docspace (
	id VARCHAR(255) NOT NULL,
	hash VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	url VARCHAR(255) NOT NULL,
	tenant_id BIGINT,
	created_at BIGINT NOT NULL,
	updated_at BIGINT,
	PRIMARY KEY (id)
);

CREATE TABLE tenants.monday_tenant_registered_boards (
	id BIGINT NOT NULL,
	access_key VARCHAR(255),
	room_id BIGINT NOT NULL,
	tenant_id BIGINT NOT NULL,
	created_at BIGINT NOT NULL,
	updated_at BIGINT,
	PRIMARY KEY (id)
);

CREATE TABLE tenants.monday_tenants (
	id BIGINT NOT NULL,
	created_at BIGINT NOT NULL,
	updated_at BIGINT,
	PRIMARY KEY (id)
);

CREATE INDEX monday_tenant_docspace_idx ON tenants.monday_tenant_docspace USING BTREE(url, tenant_id);
ALTER TABLE IF EXISTS tenants.monday_tenant_docspace ADD CONSTRAINT UK_tenant_id UNIQUE (tenant_id);
CREATE INDEX monday_tenant_boards_idx ON tenants.monday_tenant_registered_boards USING BTREE(room_id, tenant_id);
ALTER TABLE IF EXISTS tenants.monday_tenant_docspace ADD CONSTRAINT FK_docspace_monday_tenant FOREIGN KEY (tenant_id) REFERENCES tenants.monday_tenants;
ALTER TABLE IF EXISTS tenants.monday_tenant_registered_boards ADD CONSTRAINT FK_board_monday_tenant FOREIGN KEY (tenant_id) REFERENCES tenants.monday_tenants;