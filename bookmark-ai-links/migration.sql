-- Remove the CREATE DATABASE line - Spin handles database creation

CREATE TABLE IF NOT EXISTS bookmarks (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         url TEXT NOT NULL,
                                         title TEXT NOT NULL,
                                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Optional: Create an index for better query performance
CREATE INDEX IF NOT EXISTS idx_bookmarks_created_at ON bookmarks(created_at);

CREATE TABLE IF NOT EXISTS User (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      username TEXT NOT NULL UNIQUE,
                      password TEXT NOT NULL,
                      date_created DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_User_date_created ON User(date_created);
