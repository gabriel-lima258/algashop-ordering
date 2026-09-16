create table public.outbox (
    id              uuid                     not null,
    channel_name    varchar(255)             not null,
    aggregate_id    varchar(255)             not null,
    event_type      varchar(255)             not null,
    payload         json                     not null,
    created_at      timestamp with time zone not null,
    attempts        integer                  not null default 0,
    next_attempt_at timestamp with time zone not null default now(),
    last_error      text,
    failed_at       timestamp with time zone,
    primary key (id)
);