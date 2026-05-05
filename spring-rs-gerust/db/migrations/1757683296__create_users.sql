CREATE TABLE users
(
    id         uuid PRIMARY KEY,
    username      VARCHAR(255) UNIQUE NOT NULL,
    password   TEXT                NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);