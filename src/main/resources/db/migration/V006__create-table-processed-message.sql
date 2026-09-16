create table public.processed_message (
	idempotency_key uuid not null,
	processed_at timestamp with time zone not null default now(),
	primary key (idempotency_key)
);