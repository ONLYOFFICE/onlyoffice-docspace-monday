CREATE SCHEMA IF NOT EXISTS users;

CREATE TABLE users.monday_tenant_users (
	monday_id BIGINT NOT NULL,
	tenant_id BIGINT NOT NULL,
	docspace_id VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	hash VARCHAR(255) NOT NULL,
	created_at BIGINT NOT NULL,
	updated_at BIGINT,
	version BIGINT NOT NULL default 0,
	PRIMARY KEY (monday_id, tenant_id)
);

CREATE INDEX monday_tenant_users_idx ON users.monday_tenant_users (monday_id, tenant_id);
CREATE INDEX monday_tenant_docspace_users_idx ON users.monday_tenant_users USING BTREE(docspace_id);