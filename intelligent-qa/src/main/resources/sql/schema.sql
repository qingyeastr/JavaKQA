create table if not exists qa_conversation (
    id bigserial primary key,
    conversation_id varchar(64) not null unique,
    user_id varchar(64) not null,
    title varchar(200) not null,
    status varchar(20) not null,
    last_message_at timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists qa_message (
    id bigserial primary key,
    message_id varchar(64) not null unique,
    conversation_id varchar(64) not null,
    role varchar(20) not null,
    content text not null,
    status varchar(20) not null,
    model_id varchar(100),
    latency_ms integer,
    created_at timestamp not null default current_timestamp
);

create table if not exists qa_citation (
    id bigserial primary key,
    message_id varchar(64) not null,
    document_id bigint,
    chunk_id bigint,
    document_title varchar(255) not null,
    section_title varchar(255),
    snippet text,
    source_url varchar(500),
    score numeric(8,4)
);

create index if not exists idx_qa_conversation_user_id on qa_conversation(user_id, updated_at desc);
create index if not exists idx_qa_message_conversation_id on qa_message(conversation_id, created_at asc);
create index if not exists idx_qa_citation_message_id on qa_citation(message_id);