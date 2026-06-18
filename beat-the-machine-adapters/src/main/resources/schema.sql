CREATE TABLE IF NOT EXISTS challenge (
    id             TEXT    PRIMARY KEY,
    prompt         TEXT    NOT NULL,
    guesses        TEXT    NOT NULL,
    lives          INTEGER NOT NULL,
    status         TEXT    NOT NULL,
    picture_status TEXT    NOT NULL,
    picture_url    TEXT,
    difficulty     TEXT    NOT NULL,
    version        INTEGER NOT NULL
);
