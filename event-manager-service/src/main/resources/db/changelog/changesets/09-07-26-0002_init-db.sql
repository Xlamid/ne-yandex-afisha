-- liquibase formatted sql

-- changeset Xlamid:1783597905320-1
CREATE SEQUENCE IF NOT EXISTS locations_seq START WITH 1 INCREMENT BY 50;
-- rollback drop sequence locations_seq;

-- changeset Xlamid:1783597905320-2
CREATE TABLE locations
(
    id          BIGINT        NOT NULL,
    name        VARCHAR(300)  NOT NULL,
    address     VARCHAR(1000) NOT NULL,
    capacity    INTEGER       NOT NULL,
    description TEXT,
    CONSTRAINT pk_locations PRIMARY KEY (id)
);
-- rollback drop table pk_locations;

-- changeset Xlamid:1783597905320-3
ALTER TABLE locations
    ADD CONSTRAINT uc_locations_name UNIQUE (name);
--rollback alter table locations drop constraint if exists uc_locations_name;

-- changeset Xlamid:1783598790667-4
ALTER TABLE locations
    ADD CONSTRAINT chk_capacity_range CHECK (capacity >= 1 AND capacity <= 1000000);
--rollback alter table locations drop constraint if exists chk_capacity_range;