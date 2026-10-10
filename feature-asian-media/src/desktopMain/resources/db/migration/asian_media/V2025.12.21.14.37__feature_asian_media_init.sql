---- Typy

CREATE TYPE asian_media.publication_language AS ENUM (
    'KOREAN',
    'CHINESE',
    'JAPANESE'
    );

CREATE TYPE asian_media.publication_type AS ENUM (
    'MANGA',
    'LIGHT_NOVEL',
    'WEB_NOVEL',
    'PUBLISHED_NOVEL',
    'MANHWA',
    'MANHUA',
    'WEBTOON'
    );

CREATE TYPE asian_media.publication_status AS ENUM (
    'NOT_READING',
    'READING',
    'COMPLETED',
    'PLAN_TO_READ'
    );


CREATE FUNCTION asian_media.manage_publication_volumes() RETURNS trigger
    LANGUAGE plpgsql
AS
$$
BEGIN
    -- Jeśli track_progress zmienione na TRUE, dodajemy wpis
    IF NEW.track_progress = TRUE AND
       (TG_OP = 'INSERT' OR (TG_OP = 'UPDATE' AND OLD.track_progress = FALSE)) THEN

        INSERT INTO asian_media.publication_volumes (publication_id)
        VALUES (NEW.id)
        ON CONFLICT (publication_id) DO NOTHING;

        -- Jeśli track_progress zmienione na FALSE, usuwamy wpis
    ELSIF TG_OP = 'UPDATE' AND NEW.track_progress = FALSE AND OLD.track_progress = TRUE THEN
        DELETE FROM asian_media.publication_volumes WHERE publication_id = NEW.id;
    END IF;

    RETURN NEW;
END;
$$;

--------- TABLES

CREATE TABLE IF NOT EXISTS asian_media.titles
(
    id         integer                             NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    titles     text[] COLLATE pg_catalog."default" NOT NULL,
    language   asian_media.publication_language    NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE TRIGGER update_titles_modtime
    BEFORE UPDATE
    ON asian_media.titles
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();


CREATE TABLE asian_media.publications
(
    id               integer PRIMARY KEY                                NOT NULL GENERATED ALWAYS AS IDENTITY,
    title_id         integer                                            NOT NULL,
    publication_type asian_media.publication_type                       NOT NULL,
    status           asian_media.publication_status                     NOT NULL,
    track_progress   boolean                  DEFAULT false             NOT NULL,
    created_at       timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at       timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT publications_title_id_publication_type_key UNIQUE (title_id, publication_type),
    CONSTRAINT publications_title_id_fkey FOREIGN KEY (title_id)
        REFERENCES asian_media.titles (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE OR REPLACE TRIGGER update_publications_modtime
    BEFORE UPDATE
    ON asian_media.publications
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();

CREATE INDEX IF NOT EXISTS idx_publications_status
    ON asian_media.publications USING btree
        (status ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS idx_publications_title_id
    ON asian_media.publications USING btree
        (title_id ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS idx_publications_track_progress
    ON asian_media.publications USING btree
        (track_progress ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS idx_publications_type
    ON asian_media.publications USING btree
        (publication_type ASC NULLS LAST);


CREATE TABLE asian_media.publication_volumes
(
    publication_id      integer NOT NULL PRIMARY KEY,
    volumes             integer,
    translated_volumes  integer,
    chapters            integer,
    translated_chapters integer,
    original_completed  boolean                  DEFAULT false,
    created_at          timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at          timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT publication_volumes_publication_id_fkey FOREIGN KEY (publication_id)
        REFERENCES asian_media.publications (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE OR REPLACE TRIGGER update_publication_volumes_modtime
    BEFORE UPDATE
    ON asian_media.publication_volumes
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();

CREATE OR REPLACE TRIGGER manage_publication_volumes_trigger
    AFTER INSERT OR UPDATE
    ON asian_media.publications
    FOR EACH ROW
EXECUTE FUNCTION asian_media.manage_publication_volumes();
