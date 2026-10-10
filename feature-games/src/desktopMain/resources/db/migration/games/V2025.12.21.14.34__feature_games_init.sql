
CREATE TYPE games.game_status AS ENUM (
    'NOT_PLAYING',
    'WITHOUT_THE_END',
    'PLAYED',
    'TO_PLAY',
    'PLAYING'
    );

---------- TABLES
CREATE TABLE IF NOT EXISTS games.series
(
    id   integer PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY,
    name text                NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS games.games
(
    id     integer PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY,
    name   text                NOT NULL UNIQUE,
    series integer,
    status games.game_status   NOT NULL,
    CONSTRAINT games_series_fkey FOREIGN KEY (series)
        REFERENCES games.series (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS games.characters
(
    game_id                     integer NOT NULL PRIMARY KEY,
    has_distinctive_character   boolean NOT NULL,
    has_distinctive_protagonist boolean NOT NULL,
    has_distinctive_antagonist  boolean NOT NULL,
    CONSTRAINT game_characters_game_id_fkey FOREIGN KEY (game_id)
        REFERENCES games.games (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS games.play_time
(
    game_id            integer        NOT NULL PRIMARY KEY,
    completion_count   integer,
    play_time_hours    numeric(10, 2) NOT NULL,
    play_time_interval interval(2), -- aktualnie nieużywane
    CONSTRAINT game_play_time_game_id_fkey FOREIGN KEY (game_id)
        REFERENCES games.games (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS games.ratings
(
    game_id           integer NOT NULL PRIMARY KEY,
    story_rating      integer,
    gameplay_rating   integer,
    atmosphere_rating integer,
    CONSTRAINT game_ratings_game_id_fkey FOREIGN KEY (game_id)
        REFERENCES games.games (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS games.categories
(
    id   integer NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name text
);

CREATE TABLE IF NOT EXISTS games.categories_to_games
(
    category_id integer NOT NULL,
    game_id     integer NOT NULL,
    CONSTRAINT categories_to_games_pkey PRIMARY KEY (category_id, game_id),
    CONSTRAINT categories_to_games_category_id_fkey FOREIGN KEY (category_id)
        REFERENCES games.categories (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT categories_to_games_game_id_fkey FOREIGN KEY (game_id)
        REFERENCES games.games (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS games.time_played_check_time
(
    check_time timestamp without time zone NOT NULL
);

CREATE OR REPLACE VIEW games.time_played
AS
SELECT sum(play_time_hours) AS time_played
FROM games.play_time;
