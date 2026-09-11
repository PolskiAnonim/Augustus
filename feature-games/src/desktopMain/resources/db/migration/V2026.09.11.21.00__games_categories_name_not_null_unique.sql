ALTER TABLE games.categories
    ALTER COLUMN name SET NOT NULL,
    ADD CONSTRAINT categories_name_key UNIQUE (name);
