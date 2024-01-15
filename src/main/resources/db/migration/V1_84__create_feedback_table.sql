CREATE TABLE IF NOT EXISTS feedback(
    id uuid NOT NULL,
    satisfaction integer,
    feedback VARCHAR(2048),
    created timestamp without time zone,
    CONSTRAINT feedback_key PRIMARY KEY (id)
);