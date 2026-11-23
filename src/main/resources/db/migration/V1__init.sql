-- OpenAML schema, version 1.
-- Every business table has tenant_id, so one client's data never mixes with another's.

CREATE TABLE tenants (
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT        NOT NULL UNIQUE,
    status      TEXT        NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT      NOT NULL REFERENCES tenants (id),
    external_ref  TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, external_ref)
);

CREATE TABLE transactions (
    id                    BIGSERIAL PRIMARY KEY,
    tenant_id             BIGINT         NOT NULL REFERENCES tenants (id),
    external_id           TEXT           NOT NULL,
    sender_account_id     BIGINT         NOT NULL REFERENCES accounts (id),
    recipient_account_id  BIGINT         NOT NULL REFERENCES accounts (id),
    amount                NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency              CHAR(3)        NOT NULL,
    event_time            TIMESTAMPTZ    NOT NULL,
    received_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    channel               TEXT,
    metadata              JSONB          NOT NULL DEFAULT '{}'::jsonb,
    payload_hash          TEXT           NOT NULL,
    late                  BOOLEAN        NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, external_id),
    CHECK (sender_account_id <> recipient_account_id)
);

CREATE INDEX idx_tx_sender_time    ON transactions (tenant_id, sender_account_id, event_time);
CREATE INDEX idx_tx_recipient_time ON transactions (tenant_id, recipient_account_id, event_time);

CREATE TABLE processing_jobs (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         BIGINT      NOT NULL REFERENCES tenants (id),
    transaction_id    BIGINT      NOT NULL REFERENCES transactions (id),
    status            TEXT        NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING', 'LEASED', 'COMPLETED', 'FAILED')),
    attempts          INT         NOT NULL DEFAULT 0,
    available_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    lease_owner       TEXT,
    lease_expires_at  TIMESTAMPTZ,
    last_error        TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at      TIMESTAMPTZ
);

CREATE INDEX idx_jobs_pending ON processing_jobs (available_at, id) WHERE status = 'PENDING';