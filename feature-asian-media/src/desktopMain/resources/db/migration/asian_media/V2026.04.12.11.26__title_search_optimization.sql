-- 1. Tabela pomocnicza do płaskiego indeksowania tytułów
CREATE TABLE asian_media.title_variants
(
    title_id INTEGER NOT NULL,
    title    TEXT    NOT NULL,
    CONSTRAINT title_variants_title_id_fkey FOREIGN KEY (title_id)
        REFERENCES asian_media.titles (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- 2. Indeks GiST dla wyszukiwania rozmytego (trigramy)
-- GiST wspiera KNN (K-Nearest Neighbors), co pozwala na błyskawiczne ORDER BY <->
CREATE INDEX idx_title_variants_trgm ON asian_media.title_variants USING gist (title gist_trgm_ops);

-- 3. Funkcja triggera do synchronizacji
CREATE OR REPLACE FUNCTION asian_media.sync_title_variants() RETURNS trigger AS
$$
BEGIN

    IF (TG_OP = 'UPDATE' OR TG_OP = 'DELETE') THEN
        DELETE FROM asian_media.title_variants WHERE title_id = OLD.id;
    END IF;

    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        INSERT INTO asian_media.title_variants (title_id, title)
        SELECT NEW.id, unnest(NEW.titles);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. Trigger
CREATE TRIGGER trg_sync_title_variants
    AFTER INSERT OR UPDATE OR DELETE
    ON asian_media.titles
    FOR EACH ROW
EXECUTE FUNCTION asian_media.sync_title_variants();

-- 5. Wypełnienie tabeli obecnymi danymi
INSERT INTO asian_media.title_variants (title_id, title)
SELECT id, unnest(titles)
FROM asian_media.titles;
