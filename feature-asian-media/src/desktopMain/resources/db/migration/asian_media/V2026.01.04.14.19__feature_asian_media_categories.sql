CREATE TABLE asian_media.categories (
    id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name TEXT
);

CREATE TABLE asian_media.titles_to_categories(
    title_id INTEGER REFERENCES asian_media.titles,
    category_id INTEGER REFERENCES asian_media.categories,
    PRIMARY KEY (title_id, category_id)
);
