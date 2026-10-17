-- Transactional outbox. Rows are written in the same transaction as the business change and
-- relayed to Kafka by Debezium's outbox event router, which reads the WAL. Rows can therefore be
-- pruned shortly after insert; Debezium does not need them to stay.
CREATE TABLE outbox_event (
    id             UUID         PRIMARY KEY,
    aggregatetype  VARCHAR(100) NOT NULL,
    aggregateid    VARCHAR(100) NOT NULL,
    type           VARCHAR(150) NOT NULL,
    payload        JSONB        NOT NULL,
    correlation_id VARCHAR(64),
    occurred_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_outbox_event_occurred_at ON outbox_event (occurred_at);
