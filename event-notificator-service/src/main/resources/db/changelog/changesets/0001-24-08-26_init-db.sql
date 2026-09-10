-- liquibase formatted sql

-- changeset Xlamid:1787566369238-1
CREATE SEQUENCE IF NOT EXISTS notification_event_payloads_seq START WITH 1 INCREMENT BY 50;
-- rollback drop sequence notification_event_payloads_seq;

-- changeset Xlamid:1787566369238-2
CREATE TABLE notification_event_payloads
(
    id           BIGINT NOT NULL,
    message_id   UUID,
    event_id     BIGINT,
    event_type   VARCHAR(255),
    occurred_at  TIMESTAMP WITHOUT TIME ZONE,
    changed_by_id BIGINT,
    owner_id     BIGINT,
    payload      JSONB,
    CONSTRAINT pk_notification_event_payloads PRIMARY KEY (id)
);
-- rollback drop table notification_event_payloads;

-- changeset Xlamid:1787566369238-3
CREATE SEQUENCE IF NOT EXISTS notifications_seq START WITH 1 INCREMENT BY 50;
-- rollback drop sequence notifications_seq;

-- changeset Xlamid:1787566369238-4
CREATE TABLE notifications
(
    id         BIGINT NOT NULL,
    user_id    BIGINT,
    is_read    BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    read_at    TIMESTAMP WITHOUT TIME ZONE,
    payload_id BIGINT,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);
-- rollback drop table notifications;

-- changeset Xlamid:1787566369238-5
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_on_payload FOREIGN KEY (payload_id)
        REFERENCES notification_event_payloads (id);
-- rollback alter table notifications drop constraint fk_notifications_on_payload;