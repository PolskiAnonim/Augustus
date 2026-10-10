---------- API

CREATE TABLE public.api_integrations
(
    id           integer PRIMARY KEY                    NOT NULL GENERATED ALWAYS AS IDENTITY,
    name         text UNIQUE                            NOT NULL,
    enabled      boolean                  DEFAULT false NOT NULL,
    api_key      text,
    endpoint_url text,
    port         integer,
    last_sync    timestamp with time zone,
    created_at   timestamp with time zone DEFAULT now(),
    updated_at   timestamp with time zone DEFAULT now()
);

CREATE TABLE public.application_settings
(
    id       integer PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY,
    language TEXT DEFAULT 'pl'
    -- inne ustawienia UI/aplikacji w przyszłości
);
