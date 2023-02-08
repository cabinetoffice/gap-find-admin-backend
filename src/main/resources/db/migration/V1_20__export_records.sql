CREATE TABLE export_records
(
    export_id uuid NOT NULL,
    submission_id uuid NOT NULL,
    application_id bigint,
    status character varying(25) COLLATE pg_catalog."default",
    email_address character varying(100) COLLATE pg_catalog."default",
    request_date date,
    CONSTRAINT export_records_pkey PRIMARY KEY (export_id, submission_id),
    CONSTRAINT export_records_application_id_fkey FOREIGN KEY (application_id)
        REFERENCES public.grant_application (grant_application_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT export_records_submission_id_fkey FOREIGN KEY (submission_id)
        REFERENCES public.grant_submission (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)