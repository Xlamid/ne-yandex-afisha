-- liquibase formatted sql

-- changeset Xlamid:1785305312461-1
CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;
--rollback drop sequence users_seq;

-- changeset Xlamid:1785305312461-2
CREATE TABLE users
(
    id            BIGINT       NOT NULL,
    login         VARCHAR(300) NOT NULL,
    age           INTEGER      NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    role          TEXT,
    CONSTRAINT pk_users PRIMARY KEY (id)
);
--rollback drop table users;

-- changeset Xlamid:1785305312461-3
ALTER TABLE users
    ADD CONSTRAINT uc_users_login UNIQUE (login);
--rollback alter table users drop constraint uc_users_login;

-- changeset Xlamid:1785305312461-4
ALTER TABLE users
    ADD CONSTRAINT chk_age_range CHECK (age >= 18 AND age <= 150)
--rollback alter table users drop constraint chk_age_range;