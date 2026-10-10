ALTER TABLE asian_media.title_variants
    DROP CONSTRAINT title_variants_title_id_fkey,
    ADD CONSTRAINT title_variants_title_id_fkey
        FOREIGN KEY (title_id)
            REFERENCES asian_media.titles (id)
            ON DELETE CASCADE;

CREATE OR REPLACE FUNCTION asian_media.sync_title_variants() RETURNS trigger AS
$$
BEGIN
    IF (TG_OP = 'UPDATE') THEN
        DELETE FROM asian_media.title_variants WHERE title_id = OLD.id;
    END IF;

    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        INSERT INTO asian_media.title_variants (title_id, title)
        SELECT NEW.id, unnest(NEW.titles);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER trg_sync_title_variants ON asian_media.titles;
CREATE TRIGGER trg_sync_title_variants
    AFTER INSERT OR UPDATE
    ON asian_media.titles
    FOR EACH ROW
EXECUTE FUNCTION asian_media.sync_title_variants();
