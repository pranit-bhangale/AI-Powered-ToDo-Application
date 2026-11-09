CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE TABLE IF NOT EXISTS users (
    id          UUID         NOT NULL,
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

CREATE TABLE IF NOT EXISTS todos (
    id          UUID         NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    completed   BOOLEAN NOT NULL DEFAULT FALSE,
    start_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date    DATE,
    user_id     UUID NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    CONSTRAINT pk_todos PRIMARY KEY (id),
    CONSTRAINT fk_todos_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_todos_user_title UNIQUE (user_id, title)
);

CREATE INDEX IF NOT EXISTS idx_todos_user_id ON todos (user_id);
CREATE INDEX IF NOT EXISTS idx_todos_user_completed ON todos (user_id, completed);
CREATE INDEX IF NOT EXISTS idx_todos_user_created ON todos (user_id, created_at);

CREATE TABLE IF NOT EXISTS tasks (
    id          UUID         NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    completed   BOOLEAN NOT NULL DEFAULT FALSE,
    start_date  DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date    DATE,
    todo_id     UUID NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE,
    version     BIGINT DEFAULT 0,
    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT fk_tasks_todo FOREIGN KEY (todo_id) REFERENCES todos (id) ON DELETE CASCADE,
    CONSTRAINT uk_tasks_todo_title UNIQUE (todo_id, title)
);

CREATE INDEX IF NOT EXISTS idx_tasks_todo_id ON tasks (todo_id);
CREATE INDEX IF NOT EXISTS idx_tasks_todo_completed ON tasks (todo_id, completed);
CREATE INDEX IF NOT EXISTS idx_tasks_todo_start_date ON tasks (todo_id, start_date);
CREATE INDEX IF NOT EXISTS idx_tasks_todo_end_date ON tasks (todo_id, end_date);
