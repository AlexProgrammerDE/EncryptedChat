CREATE TABLE IF NOT EXISTS users
(
    name       TEXT NOT NULL UNIQUE PRIMARY KEY,
    id         TEXT NOT NULL UNIQUE,
    public_key TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS groups
(
    id     TEXT NOT NULL PRIMARY KEY UNIQUE,
    name   TEXT NOT NULL,
    key_id TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS group_members
(
    group_id TEXT    NOT NULL,
    user_id  TEXT    NOT NULL,
    admin    INTEGER NOT NULL,
    FOREIGN KEY (group_id) REFERENCES groups (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE TABLE IF NOT EXISTS group_messages
(
    group_id TEXT NOT NULL PRIMARY KEY UNIQUE,
    user_id  TEXT NOT NULL,
    message_encrypted  TEXT NOT NULL,
    message_signature  TEXT NOT NULL,
    FOREIGN KEY (group_id) REFERENCES groups (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
