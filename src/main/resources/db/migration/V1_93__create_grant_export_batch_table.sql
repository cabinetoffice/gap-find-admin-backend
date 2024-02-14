-- Create the new grant_export_batch table
CREATE TABLE IF NOT EXISTS public.grant_export_batch
(
    export_batch_id uuid NOT NULL,
    application_id bigint NOT NULL,
    status character varying(25) COLLATE pg_catalog."default" NOT NULL,
    email_address character varying(100) COLLATE pg_catalog."default",
    created timestamp without time zone,
    created_by integer NOT NULL,
    last_updated timestamp without time zone,
    location character varying(2048) COLLATE pg_catalog."default",

    CONSTRAINT grant_export_batch_pkey PRIMARY KEY (export_batch_id),
    CONSTRAINT grant_export_batch_application_id_fkey FOREIGN KEY (application_id)
        REFERENCES public.grant_application (grant_application_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT grant_export_batch_created_by_fkey FOREIGN KEY (created_by)
        REFERENCES public.grant_admin (grant_admin_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Populate the new table using the existing grant_export table as a base
INSERT INTO grant_export_batch (export_batch_id, application_id, status, email_address, created, created_by, last_updated)
SELECT
    export_batch_id,
    MIN(application_id) AS application_id,
    'NOT_GENERATED' AS status,
    MIN(email_address) AS email_address,
    MIN(created) AS created,
    MIN(created_by) AS created_by,
    MIN(last_updated) AS last_updated
FROM grant_export
GROUP BY export_batch_id
ORDER BY export_batch_id;

-- Add a foreign key constraint to grant_export so there must be a corresponding entry in the grant_export_batch table
ALTER TABLE grant_export
ADD CONSTRAINT fk_grant_export_batch
FOREIGN KEY (export_batch_id)
REFERENCES grant_export_batch(export_batch_id);
