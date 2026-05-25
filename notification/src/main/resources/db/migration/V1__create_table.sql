CREATE TABLE processed_events (
    event_id BIGINT PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_processed_events_status ON processed_events(status);