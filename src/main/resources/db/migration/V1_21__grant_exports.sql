DROP TABLE IF EXISTS public.export_records;

CREATE TABLE IF NOT EXISTS public.grant_export
(
    export_batch_id uuid NOT NULL,
    submission_id uuid NOT NULL,
    application_id bigint NOT NULL,
    status character varying(25) COLLATE pg_catalog."default" NOT NULL,
    email_address character varying(100) COLLATE pg_catalog."default",
    created date,
    created_by integer NOT NULL,
    last_updated date,
    location character varying(2048) COLLATE pg_catalog."default",
    CONSTRAINT grant_export_pkey PRIMARY KEY (export_batch_id, submission_id),
    CONSTRAINT grant_export_application_id_fkey FOREIGN KEY (application_id)
        REFERENCES public.grant_application (grant_application_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT grant_export_created_by_fkey FOREIGN KEY (created_by)
        REFERENCES public.grant_admin (grant_admin_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT grant_export_submission_id_fkey FOREIGN KEY (submission_id)
        REFERENCES public.grant_submission (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)