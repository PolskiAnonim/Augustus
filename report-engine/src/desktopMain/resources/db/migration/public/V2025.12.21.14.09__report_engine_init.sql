-- dodatkowa funkcja do aktualizacji kolumny updated_at

CREATE FUNCTION public.update_modified_column() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TYPE public.filter_config AS
(
    column_name text,
    config      jsonb
);

CREATE TYPE public.sort_direction AS ENUM (
    'ASCENDING',
    'DESCENDING'
    );

CREATE TYPE public.sort_configuration AS
(
    column_name    text,
    sort_direction public.sort_direction
);

CREATE TABLE public.report_configurations
(
    id              integer                                            NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name            text                                               NOT NULL,
    report_name     text                                               NOT NULL,
    description     text,
    is_default      boolean                                            NOT NULL,
    sort_order      public.sort_configuration[]                        NOT NULL,
    visible_columns text[]                                             NOT NULL,
    column_order    text[]                                             NOT NULL,
    page_size       int8                                               NOT NULL,
    filters         public.filter_config[],
    created_at      timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT report_configurations_name_type_unique UNIQUE (name, report_name)
);

CREATE OR REPLACE TRIGGER update_report_configurations_modtime
    BEFORE UPDATE
    ON public.report_configurations
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();
