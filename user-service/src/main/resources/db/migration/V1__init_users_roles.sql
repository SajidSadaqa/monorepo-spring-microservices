-- ensure a UUID generator exists (pick one)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
/* or: CREATE EXTENSION IF NOT EXISTS pgcrypto; */

CREATE TABLE roles (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),  -- or gen_random_uuid()
  name TEXT NOT NULL UNIQUE
);


create table if not exists users (
  id           uuid primary key,
  username     varchar(30)  not null,
  email        varchar(120) not null,
  password_hash varchar(255) not null,
  enabled      boolean not null,
  created_at   timestamp not null,
  updated_at   timestamp not null,
  constraint ux_users_username unique (username),
  constraint ux_users_email    unique (email)
);

create table if not exists user_roles (
  user_id uuid not null references users(id) on delete cascade,
  role_id uuid not null references roles(id) on delete cascade,
  primary key (user_id, role_id)
);

create table if not exists refresh_tokens (
  id          uuid primary key,
  user_id     uuid not null references users(id) on delete cascade,
  jti         varchar(36) not null unique,
  expires_at  timestamp not null,
  revoked     boolean not null default false,
  created_at  timestamp not null default now(),
  replaced_by varchar(36)
);

create index if not exists ix_refresh_tokens_user on refresh_tokens(user_id);
create index if not exists ix_refresh_tokens_revoked on refresh_tokens(revoked);
