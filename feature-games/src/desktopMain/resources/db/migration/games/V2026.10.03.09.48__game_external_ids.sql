CREATE TYPE games.source_site AS ENUM (
    'STEAM'
    );

-- Identyfikatory gry na zewnętrznych stronach, jak asian_media.title_external_ids. Jedna gra może mieć
-- kilka appid ze Steama (np. oryginał i remaster pod inną nazwą prowadzone jako jedna gra), więc unikalna
-- jest para (site, external_id), a nie (game_id, site). external_id jest tekstem, jak przy nowelkach.
CREATE TABLE games.game_external_ids
(
    game_id     integer           NOT NULL,
    site        games.source_site NOT NULL,
    external_id text              NOT NULL,
    CONSTRAINT game_external_ids_pkey PRIMARY KEY (site, external_id),
    CONSTRAINT game_external_ids_game_id_fkey FOREIGN KEY (game_id)
        REFERENCES games.games (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_game_external_ids_game_id
    ON games.game_external_ids USING btree
        (game_id ASC NULLS LAST);
