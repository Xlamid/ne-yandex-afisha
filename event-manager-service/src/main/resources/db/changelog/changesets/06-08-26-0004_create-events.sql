-- liquibase formatted sql

-- changeset Xlamid:1786015491528-1
CREATE SEQUENCE IF NOT EXISTS events_seq START WITH 1 INCREMENT BY 50;
-- rollback drop sequence if exists events_seq;

-- changeset Xlamid:1786015491528-2
CREATE TABLE events
(
    id               BIGINT                      NOT NULL,
    name             TEXT                        NOT NULL,
    start_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    cost             INTEGER,
    duration_minutes INTEGER,
    max_places       INTEGER,
    occupied_places  INTEGER,
    status           VARCHAR(255)                NOT NULL,
    location_id      BIGINT,
    user_id          BIGINT,
    CONSTRAINT pk_events PRIMARY KEY (id)
);
-- rollback drop table if exists events;

-- changeset Xlamid:1786015491528-3
ALTER TABLE events
    ADD CONSTRAINT fk_events_on_location FOREIGN KEY (location_id) REFERENCES locations (id);
-- rollback alter table events drop constraint if exists fk_events_on_location;

-- changeset Xlamid:1786015491528-4
ALTER TABLE events
    ADD CONSTRAINT fk_events_on_user FOREIGN KEY (user_id) REFERENCES users (id);
-- rollback alter table events drop constraint if exists fk_events_on_user;

-- changeset Xlamid:1786015491528-5
ALTER TABLE events
    ADD CONSTRAINT chk_cost CHECK (cost >= 1);
-- rollback alter table events drop constraint if exists chk_cost;

-- changeset Xlamid:1786015491528-6
ALTER TABLE events
    ADD CONSTRAINT chk_duration CHECK (duration_minutes >= 30);
-- rollback alter table events drop constraint if exists chk_duration;

-- changeset Xlamid:1786015491528-7
ALTER TABLE events
    ADD CONSTRAINT chk_max_places CHECK (max_places >= 1);
-- rollback alter table events drop constraint if exists chk_max_places;