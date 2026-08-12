-- liquibase formatted sql

-- changeset Xlamid:1786016911006-1
CREATE SEQUENCE IF NOT EXISTS registrations_seq START WITH 1 INCREMENT BY 50;
-- rollback drop sequence if exists registrations_seq;

-- changeset Xlamid:1786016911006-2
CREATE TABLE registrations
(
    id          BIGINT                      NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_canceled BOOLEAN                     NOT NULL DEFAULT FALSE,
    user_id     BIGINT,
    event_id    BIGINT,
    CONSTRAINT pk_registrations PRIMARY KEY (id)
);
-- rollback drop table if exists registrations;

-- changeset Xlamid:1786016911006-3
ALTER TABLE registrations
    ADD CONSTRAINT fk_registrations_on_event FOREIGN KEY (event_id) REFERENCES events (id);
-- rollback alter table registrations drop constraint if exists fk_registrations_on_event;

-- changeset Xlamid:1786016911006-4
ALTER TABLE registrations
    ADD CONSTRAINT fk_registrations_on_user FOREIGN KEY (user_id) REFERENCES users (id);
-- rollback alter table registrations drop constraint if exists fk_registrations_on_user;