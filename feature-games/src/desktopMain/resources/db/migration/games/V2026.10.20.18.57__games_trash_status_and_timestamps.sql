-- NOT_PLAYING to w praktyce gry odrzucone (zignorowane na Steamie), więc nazwa jak w asian_media.
ALTER TYPE games.game_status RENAME VALUE 'NOT_PLAYING' TO 'TRASH';

-- Istniejące gry dostają czas tej migracji, tak jak books i asian_media przy ich wprowadzeniu.
ALTER TABLE games.games
    ADD COLUMN created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ADD COLUMN updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL;

-- Trigger zamiast ustawiania kolumny w formularzu, bo gry zmienia też GamesApi.
CREATE TRIGGER update_games_modtime
    BEFORE UPDATE
    ON games.games
    FOR EACH ROW
EXECUTE FUNCTION public.update_modified_column();
