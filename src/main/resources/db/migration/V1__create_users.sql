CREATE TABLE users (
    id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users
       PRIMARY KEY (id),

    CONSTRAINT uk_users_email
       UNIQUE (email),

    CONSTRAINT ck_users_role
       CHECK (role IN ('CLIENT', 'ANALYST', 'ADMIN')),

    CONSTRAINT ck_users_email_lowercase
       CHECK (email = LOWER(email))
);

CREATE INDEX idx_users_role
    ON users (role);

CREATE INDEX idx_users_active
    ON users (active);