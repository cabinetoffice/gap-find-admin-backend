CREATE TABLE IF NOT EXISTS feedback(
    id SERIAL NOT NULL,
    satisfaction integer,
    comment VARCHAR(2048),
    journey VARCHAR(100),
    created timestamp without time zone,
    CONSTRAINT feedback_key PRIMARY KEY (id)
);