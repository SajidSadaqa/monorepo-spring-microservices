-- V2__seed_roles.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO roles (id, name) VALUES (uuid_generate_v4(), 'ROLE_USER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name) VALUES (uuid_generate_v4(), 'ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

