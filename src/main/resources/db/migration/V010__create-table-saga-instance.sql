create table public.saga_instance (
    saga_id      uuid                     not null,
    saga_type    varchar(50)              not null,
    aggregate_id varchar(100)             not null,
    status       varchar(30)              not null,
    step         varchar(50)              not null,
    failure      varchar(50),
    created_at   timestamp with time zone not null,
    updated_at   timestamp with time zone not null,
    version      bigint                   not null,
    primary key (saga_id),
    constraint uk_saga_instance_type_aggregate unique (saga_type, aggregate_id)
);

create index idx_saga_instance_active
    on public.saga_instance (saga_type, status)
    where status in ('RUNNING', 'COMPENSATING');