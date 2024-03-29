CREATE TABLE IF NOT EXISTS event_stream.completion_statistics
(
    id bigint NOT NULL,
    user_sub character varying(128) COLLATE pg_catalog."default" NOT NULL,
    funding_organisation_id bigint,
    object_type character varying(128) COLLATE pg_catalog."default" NOT NULL,
    object_id character varying COLLATE pg_catalog."default" NOT NULL,
    total_alive_time bigint NOT NULL,
    time_worked_on bigint NOT NULL,
    object_completed timestamp(6) with time zone,
    created timestamp(6) with time zone NOT NULL,
    CONSTRAINT completion_statistics_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS completion_statistics_funding_org_id_index
    ON event_stream.completion_statistics USING btree
    (funding_organisation_id ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS completion_statistics_object_id_index
    ON event_stream.completion_statistics USING btree
    (object_id COLLATE pg_catalog."default" ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS completion_statistics_object_id_user_sub_index
    ON event_stream.completion_statistics USING btree
    (object_id COLLATE pg_catalog."default" ASC NULLS LAST, user_sub COLLATE pg_catalog."default" ASC NULLS LAST);

CREATE INDEX IF NOT EXISTS completion_statistics_user_sub_index
    ON event_stream.completion_statistics USING btree
    (user_sub COLLATE pg_catalog."default" ASC NULLS LAST);



-- Sequence creation
CREATE SEQUENCE IF NOT EXISTS event_stream.completion_statistics_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;
