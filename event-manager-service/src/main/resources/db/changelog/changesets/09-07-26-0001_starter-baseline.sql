--liquibase formatted sql

--changeset xlamid:09-07-26-0001_starter-baseline
CREATE TABLE starter_bootstrap_marker (
    id         BIGINT                      NOT NULL,
    note       VARCHAR(255)                NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_starter_bootstrap_marker PRIMARY KEY (id)
);

INSERT INTO starter_bootstrap_marker (id, note, created_at)
VALUES (1, 'starter baseline applied', '2025-01-01 00:00:00');

--rollback DROP TABLE starter_bootstrap_marker;