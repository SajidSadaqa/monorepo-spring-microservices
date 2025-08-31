create table if not exists roles (
  id   bigint generated always as identity primary key,
  name varchar(50) not null unique
);

create table if not exists users (
  id         bigint generated always as identity primary key,
  username   varchar(50) not null unique,
  email      varchar(255) not null unique,
  password   varchar(255) not null,
  enabled    boolean not null default true,
  created_at timestamp not null default now()
);

create table if not exists user_roles (
  user_id bigint not null references users(id) on delete cascade,
  role_id bigint not null references roles(id) on delete cascade,
  primary key (user_id, role_id)
);

create table if not exists refresh_tokens (
  id         bigint generated always as identity primary key,
  user_id    bigint not null references users(id) on delete cascade,
  jti        varchar(64) not null unique,
  revoked    boolean not null default false,
  created_at timestamp not null default now(),
  expires_at timestamp not null
);
create index if not exists idx_refresh_tokens_user on refresh_tokens(user_id);
create index if not exists idx_refresh_tokens_expires on refresh_tokens(expires_at);
