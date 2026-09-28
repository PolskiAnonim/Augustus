-- Import listy lektur z NovelUpdates do asian_media.
--
-- Skąd JSON: novelupdates_list_via_series_finder.js. Małą listę (do paru tysięcy serii - większej
-- serwer nie zdąży wyrenderować) można też wziąć prosto ze strony listy, wklejając w konsolę:
--
--   copy(JSON.stringify([...document.querySelectorAll('tr.rl_links')].map(tr => ({ sid: tr.dataset.sid, title: tr.dataset.title }))))
--
-- Tytuł bierzemy z data-title, bo tekst linku bywa skracany. Plik musi leżeć na dysku D: - czyta go
-- serwer (pg_read_file), a usługa PostgreSQL nie ma dostępu do katalogów w C:\Users.
--
-- Uruchomienie:
--   psql -h localhost -U postgres -d augustus -v file=D:/nu/trash.json -v status=NOT_READING -f import_novelupdates_list.sql
-- status jest opcjonalny (domyślnie NOT_READING). Z -v dry_run=1 wszystko się wykona i wypisze,
-- ale na końcu jest ROLLBACK.
--
-- Serie dopasowujemy tylko dokładnie (bez wielkości liter) do title_variants - trigramy przy
-- tysiącach podobnych tytułów zjadłyby różne serie. Seria już podpięta po sid jest pomijana, więc
-- skrypt można puszczać wielokrotnie. Wszystko, co niejednoznaczne, idzie do przejrzenia: nie jest
-- ani wstawiane, ani podpinane, i przy następnym uruchomieniu wypadnie tak samo.
--
-- Kilka serii o tym samym tytule na liście to nic podejrzanego - NovelUpdates ma np. trzy różne
-- "Addicted To You" - więc każda dostaje własny tytuł. Do przejrzenia idą dopiero wtedy, gdy
-- trafiają w tytuł, który już jest w bazie, bo nie wiadomo, którą z nich do niego podpiąć.
--
-- Typ i język nowych tytułów biorą się z pól type i org, jeśli JSON je ma (series finder ma, strona
-- listy nie). Języki spoza enuma i brak danych dają CHINESE, brak typu WEB_NOVEL - te same wartości
-- domyślne co NovelUpdatesParser. Istniejącym tytułom nic poza sid się nie zmienia.

\set ON_ERROR_STOP on
\if :{?status}
\else
    \set status NOT_READING
\endif

BEGIN;

-- Literówka w statusie ma wywalić skrypt, zanim cokolwiek zrobi.
SELECT :'status'::asian_media.publication_status AS status;

CREATE TEMP TABLE nu_list ON COMMIT DROP AS
SELECT DISTINCT ON (sid) sid,
                         btrim(title)                              AS title,
                         CASE org
                             WHEN 'kr' THEN 'KOREAN'
                             WHEN 'jp' THEN 'JAPANESE'
                             ELSE 'CHINESE'
                             END::asian_media.publication_language AS language,
                         coalesce(type, 'WEB_NOVEL')::asian_media.publication_type AS type
FROM jsonb_to_recordset(pg_read_file(:'file')::jsonb) AS x (sid text, title text, org text, type text)
WHERE btrim(title) <> '';

CREATE TEMP TABLE nu_plan ON COMMIT DROP AS
WITH pending AS (
    SELECT l.sid, l.title, l.language, l.type
    FROM nu_list l
    WHERE NOT EXISTS (SELECT 1
                      FROM asian_media.title_external_ids e
                      WHERE e.site = 'NOVEL_UPDATES'
                        AND e.external_id = l.sid)
),
     matches AS (
         SELECT p.sid,
                array_agg(DISTINCT v.title_id ORDER BY v.title_id) AS title_ids,
                bool_or(lower(t.titles[1]) = lower(p.title))       AS main_title_match,
                min(t.language)                                     AS title_language
         FROM pending p
                  JOIN asian_media.title_variants v ON lower(v.title) = lower(p.title)
                  JOIN asian_media.titles t ON t.id = v.title_id
         GROUP BY p.sid
     ),
     counted AS (
         SELECT p.sid,
                p.title,
                p.language,
                p.type,
                m.title_ids,
                m.main_title_match,
                m.title_language,
                count(*) OVER (PARTITION BY m.title_ids) AS same_match_on_list
         FROM pending p
                  LEFT JOIN matches m USING (sid)
     )
SELECT c.sid,
       c.title,
       c.language,
       c.type,
       c.title_ids,
       CASE
           WHEN cardinality(c.title_ids) > 1 THEN 'pasuje do kilku tytułów'
           WHEN c.title_ids IS NOT NULL AND c.same_match_on_list > 1
               THEN 'kilka serii z listy pasuje do jednego tytułu'
           -- Alternatywne tytuły bywają ogólnikowe ("Nobody", "Forbidden Love") i trafiają w cudze
           -- serie; inny język to najpewniejszy znak, że tak jest.
           WHEN NOT c.main_title_match AND c.title_language <> c.language
               THEN 'tytuł alternatywny, inny język'
           WHEN EXISTS (SELECT 1
                        FROM asian_media.title_external_ids e
                        WHERE e.title_id = c.title_ids[1]
                          AND e.site = 'NOVEL_UPDATES')
               THEN 'tytuł ma już inny sid'
           END AS review_reason
FROM counted c;

-- Dokładne trafienie w istniejący tytuł: tylko podpinamy sid.
INSERT INTO asian_media.title_external_ids (title_id, site, external_id)
SELECT title_ids[1], 'NOVEL_UPDATES', sid
FROM nu_plan
WHERE review_reason IS NULL
  AND title_ids IS NOT NULL;

-- Brak trafienia: nowy tytuł, publikacja i sid. Tytuły nie są unikalne, więc wstawionego wiersza nie
-- da się po nich połączyć z sid - identyfikatory bierzemy z sekwencji z góry, po jednym na serię.
CREATE TEMP TABLE nu_new ON COMMIT DROP AS
SELECT nextval(pg_get_serial_sequence('asian_media.titles', 'id')) AS id, sid, title, language, type
FROM nu_plan
WHERE review_reason IS NULL
  AND title_ids IS NULL;

INSERT INTO asian_media.titles (id, titles, language)
    OVERRIDING SYSTEM VALUE
SELECT id, ARRAY [title], language
FROM nu_new;

INSERT INTO asian_media.publications (title_id, publication_type, status, track_progress)
SELECT id, type, :'status', false
FROM nu_new;

INSERT INTO asian_media.title_external_ids (title_id, site, external_id)
SELECT id, 'NOVEL_UPDATES', sid
FROM nu_new;

SELECT 'już podpięte, pominięte' AS wynik, (SELECT count(*) FROM nu_list) - (SELECT count(*) FROM nu_plan) AS serii
UNION ALL
SELECT 'podpięte do istniejących tytułów', count(*)
FROM nu_plan
WHERE review_reason IS NULL
  AND title_ids IS NOT NULL
UNION ALL
SELECT 'dodane jako nowe tytuły', count(*)
FROM nu_plan
WHERE review_reason IS NULL
  AND title_ids IS NULL
UNION ALL
SELECT 'do przejrzenia', count(*)
FROM nu_plan
WHERE review_reason IS NOT NULL;

SELECT review_reason AS powod, sid, title AS tytul_na_liscie, title_ids AS pasujace_tytuly
FROM nu_plan
WHERE review_reason IS NOT NULL
ORDER BY review_reason, lower(title);

\if :{?dry_run}
ROLLBACK;
\else
COMMIT;
\endif
