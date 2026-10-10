-- Flyway tworzył te schematy sam, z listy `schemas` w konfiguracji.
-- OctaviusMigrator tworzy wyłącznie schemat tabeli historii, więc schematy aplikacji
-- muszą powstać tutaj - przed pierwszą migracją, która się do któregoś z nich odwołuje.
CREATE SCHEMA IF NOT EXISTS asian_media;
CREATE SCHEMA IF NOT EXISTS games;
CREATE SCHEMA IF NOT EXISTS books;
