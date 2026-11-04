alter table public.outbox add column correlation_id varchar(255);
alter table public.outbox add column reply_topic varchar(255);