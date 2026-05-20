create table if not exists sys_user (
    id bigserial primary key,
    username varchar(50) not null unique,
    password varchar(100) not null,
    role_code varchar(30) not null,
    status smallint not null default 1,
    last_login_time timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists system_config (
    id bigserial primary key,
    config_key varchar(100) not null unique,
    config_name varchar(100) not null,
    config_group varchar(50) not null,
    config_value text not null,
    value_type varchar(20) not null,
    description varchar(255),
    is_enabled boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists operation_log (
    id bigserial primary key,
    operator_id bigint,
    module_name varchar(50) not null,
    operation_type varchar(50) not null,
    operation_content text not null,
    created_at timestamp not null default current_timestamp
);

create index if not exists idx_system_config_group on system_config(config_group);
create index if not exists idx_operation_log_created_at on operation_log(created_at desc);