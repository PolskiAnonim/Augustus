-- Jednorazowe dopięcie appid ze Steama do gier, które były zignorowane, zanim wtyczka zaczęła
-- podsłuchiwać SteamDB.
--
-- Skąd JSON: steam_ignored_apps.mjs. Plik musi leżeć na dysku D: - czyta go serwer (pg_read_file),
-- a usługa PostgreSQL nie ma dostępu do katalogów w C:\Users.
--
-- Uruchomienie:
--   psql -h localhost -U postgres -d augustus -v file=D:/steam/ignored_apps.json -f link_steam_ignored_apps.sql
-- Z -v dry_run=1 wszystko się wykona i wypisze, ale na końcu jest ROLLBACK.
--
-- Dopasowanie jak w POST /api/games/steam/ignore: nazwy po samych literach i cyfrach, bez wielkości
-- liter, dokładnie jedna gra. Tylko podpina appid - nie dodaje gier i nie zmienia statusów; to, czego nie
-- dopiął, wypisuje. appid już podpięty jest pomijany, więc skrypt można puszczać wielokrotnie.

\set ON_ERROR_STOP on

BEGIN;

CREATE TEMP TABLE steam_ignored ON COMMIT DROP AS
SELECT appid::text                                       AS app_id,
       name,
       lower(regexp_replace(name, '[^[:alnum:]]+', '', 'g')) AS name_key,
       type,
       reason
FROM jsonb_to_recordset(pg_read_file(:'file')::jsonb) AS x (appid integer, name text, type integer, reason integer);

CREATE TEMP TABLE steam_plan ON COMMIT DROP AS
SELECT i.app_id,
       i.name,
       i.type,
       i.reason,
       (SELECT array_agg(g.id ORDER BY g.id)
        FROM games.games g
        WHERE lower(regexp_replace(g.name, '[^[:alnum:]]+', '', 'g')) = i.name_key) AS game_ids
FROM steam_ignored i
WHERE i.name IS NOT NULL
  AND NOT EXISTS (SELECT 1
                  FROM games.game_external_ids e
                  WHERE e.site = 'STEAM'
                    AND e.external_id = i.app_id);

INSERT INTO games.game_external_ids (game_id, site, external_id)
SELECT game_ids[1], 'STEAM', app_id
FROM steam_plan
WHERE cardinality(game_ids) = 1;

SELECT 'już podpięte, pominięte' AS wynik,
       (SELECT count(*) FROM steam_ignored WHERE name IS NOT NULL) - (SELECT count(*) FROM steam_plan) AS aplikacji
UNION ALL
SELECT 'bez nazwy (zdjęte ze sklepu), pominięte', count(*)
FROM steam_ignored
WHERE name IS NULL
UNION ALL
SELECT 'podpięte do gier', count(*)
FROM steam_plan
WHERE cardinality(game_ids) = 1
UNION ALL
SELECT 'pasują do kilku gier', count(*)
FROM steam_plan
WHERE cardinality(game_ids) > 1
UNION ALL
SELECT 'nie ma w bazie', count(*)
FROM steam_plan
WHERE game_ids IS NULL;

-- Typy tak, jak zwraca je GetItems; podpisane tylko te, które widać po nazwach.
SELECT CASE type
           WHEN 0 THEN 'gra'
           WHEN 1 THEN 'demo'
           WHEN 4 THEN 'DLC'
           WHEN 6 THEN 'program'
           WHEN 10 THEN 'sprzęt'
           WHEN 11 THEN 'soundtrack'
           ELSE 'typ ' || type
           END  AS nie_ma_w_bazie,
       count(*) AS aplikacji
FROM steam_plan
WHERE game_ids IS NULL
GROUP BY type
ORDER BY count(*) DESC;

SELECT p.app_id, p.name AS nazwa_na_steamie, array_agg(g.name ORDER BY g.id) AS pasujace_gry
FROM steam_plan p
         JOIN games.games g ON g.id = ANY (p.game_ids)
WHERE cardinality(p.game_ids) > 1
GROUP BY p.app_id, p.name
ORDER BY lower(p.name);

-- Zignorowane na Steamie, a u nas z innym statusem. Powód 2 to „grałem gdzie indziej”.
SELECT g.status, p.reason AS powod_na_steamie, count(*) AS gier
FROM steam_plan p
         JOIN games.games g ON g.id = p.game_ids[1]
WHERE cardinality(p.game_ids) = 1
  AND g.status <> 'NOT_PLAYING'
GROUP BY g.status, p.reason
ORDER BY g.status, p.reason;

\if :{?dry_run}
ROLLBACK;
\else
COMMIT;
\endif
