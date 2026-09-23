CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE notifications.inbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id UUID NOT NULL REFERENCES identity.users(id),
    notification_type VARCHAR(80) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    target_type VARCHAR(80),
    target_id UUID,
    dedupe_key VARCHAR(180) NOT NULL UNIQUE,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX notifications_inbox_recipient_created_idx
    ON notifications.inbox (recipient_user_id, created_at DESC);

CREATE INDEX notifications_inbox_unread_idx
    ON notifications.inbox (recipient_user_id, read_at)
    WHERE read_at IS NULL;
