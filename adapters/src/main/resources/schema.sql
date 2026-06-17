CREATE TABLE IF NOT EXISTS challenge (
    id           TEXT PRIMARY KEY,
    prompt       TEXT NOT NULL,
    guesses      TEXT NOT NULL,
    lives        INT  NOT NULL,
    status       TEXT NOT NULL,
    picture_status TEXT NOT NULL,
    picture_url  TEXT,
    difficulty   TEXT NOT NULL,
    version      INT  NOT NULL
);
