CREATE TYPE asian_media.source_site AS ENUM (
    'NOVEL_UPDATES',
    'MANGA_UPDATES'
    );

-- Identyfikatory serii na zewnętrznych stronach. Wiszą na tytule, a nie na publikacji, bo LN i WN
-- trafiają do jednego tytułu - i z tego samego powodu jeden tytuł może mieć kilka identyfikatorów
-- z tej samej strony, więc unikalna jest para (site, external_id), a nie (title_id, site).
-- external_id jest tekstem, bo NovelUpdates numeruje liczbami, a MangaUpdates w base36.
CREATE TABLE asian_media.title_external_ids
(
    title_id    integer                 NOT NULL,
    site        asian_media.source_site NOT NULL,
    external_id text                    NOT NULL,
    CONSTRAINT title_external_ids_pkey PRIMARY KEY (site, external_id),
    CONSTRAINT title_external_ids_title_id_fkey FOREIGN KEY (title_id)
        REFERENCES asian_media.titles (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_title_external_ids_title_id
    ON asian_media.title_external_ids USING btree
        (title_id ASC NULLS LAST);
