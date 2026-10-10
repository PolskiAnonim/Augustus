CREATE TABLE books.authors
(
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       TEXT NOT NULL UNIQUE,
    sort_name  TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN books.authors.name is 'Pełne imię i nazwisko autora (do wyświetlania).';

COMMENT ON COLUMN books.authors.sort_name is 'Nazwisko w formacie do sortowania (np. Nazwisko, Imię).';

CREATE INDEX idx_authors_sort_name
    ON books.authors (sort_name);

CREATE TYPE books.reading_status AS ENUM
    ('NOT_READING', 'READING', 'COMPLETED', 'PLAN_TO_READ');

CREATE TABLE books.books
(
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title_pl   TEXT NOT NULL,
    title_eng  TEXT,
    status     books.reading_status DEFAULT 'NOT_READING'::books.reading_status,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE books.book_to_authors
(
    book_id   INTEGER NOT NULL
        REFERENCES books.books
            ON DELETE CASCADE,
    author_id INTEGER NOT NULL
        REFERENCES books.authors
            ON DELETE RESTRICT,
    PRIMARY KEY (book_id, author_id)
);
