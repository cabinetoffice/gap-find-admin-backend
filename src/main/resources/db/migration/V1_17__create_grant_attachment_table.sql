CREATE TABLE IF NOT EXISTS public.grant_attachment
(
    grant_attachment_id uuid NOT NULL,
    created timestamp without time zone NOT NULL,
    filename character varying(255) COLLATE pg_catalog."default" NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    location text COLLATE pg_catalog."default" NOT NULL,
    question_id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    status character varying(255) COLLATE pg_catalog."default" NOT NULL,
    version integer NOT NULL,
    created_by bigint,
    submission_id uuid,
    CONSTRAINT grant_attachment_pkey PRIMARY KEY (grant_attachment_id),
    CONSTRAINT fkauex62pbrqwu8rh0g28l8phq9 FOREIGN KEY (created_by)
        REFERENCES public.grant_applicant (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fkq3von08d8ihi8rxngekli01l0 FOREIGN KEY (submission_id)
        REFERENCES public.grant_submission (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)