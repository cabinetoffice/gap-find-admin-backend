CREATE TABLE IF NOT EXISTS public.spotlight_submission (
	id uuid NOT NULL,
	grant_mandatory_questions_id uuid NULL,
  grant_scheme int4 NULL,
	status varchar(255) NOT NULL,
	last_send_attempt timestamp NULL,
	version int4 NULL,
	created timestamp NOT NULL,
	created_by BIGINT NOT NULL,
  last_updated timestamp NOT NULL,
	last_updated_by BIGINT NOT NULL,
	CONSTRAINT spotlight_submission_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.spotlight_batch (
	id uuid NOT NULL,
	status varchar(255) NOT NULL,
	last_send_attempt timestamp NULL,
	version int4 NULL,
	created timestamp NOT NULL,
	created_by BIGINT NOT NULL,
  last_updated timestamp NOT NULL,
	last_updated_by BIGINT NOT NULL,
	CONSTRAINT spotlight_batch_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.spotlight_batch_submission (
	spotlight_submission_id uuid NOT NULL,
	spotlight_batch_id uuid NOT NULL,
	CONSTRAINT spotlight_batch_submission_pkey PRIMARY KEY (spotlight_submission_id, spotlight_batch_id)
);

ALTER TABLE public.spotlight_submission
ADD CONSTRAINT spotlight_submission_grant_mandatory_questions_id_to_grant_mandatory_questions_table_fk
FOREIGN KEY (grant_mandatory_questions_id)
REFERENCES public.grant_mandatory_questions(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.spotlight_submission
ADD CONSTRAINT spotlight_submission_grant_scheme_to_grant_scheme_table_fk
FOREIGN KEY (grant_scheme)
REFERENCES public.grant_scheme(grant_scheme_id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.spotlight_submission
ADD CONSTRAINT spotlight_submission_created_by_to_grant_applicant_table_fk
FOREIGN KEY (created_by)
REFERENCES public.grant_applicant(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.spotlight_submission
ADD CONSTRAINT spotlight_submission_last_updated_by_to_grant_applicant_table_fk
FOREIGN KEY (last_updated_by)
REFERENCES public.grant_applicant(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

CREATE INDEX spotlight_submission_grant_mandatory_questions_id_to_grant_mandatory_questions_table_idx ON spotlight_submission (grant_mandatory_questions_id);
CREATE INDEX spotlight_submission_grant_scheme_to_grant_scheme_table_idx ON spotlight_submission (grant_scheme);
CREATE INDEX spotlight_submission_created_by_to_grant_applicant_table_idx ON spotlight_submission (created_by);
CREATE INDEX spotlight_submission_last_updated_by_to_grant_applicant_table_idx ON spotlight_submission (last_updated_by);

ALTER TABLE public.spotlight_batch
ADD CONSTRAINT spotlight_batch_created_by_to_grant_applicant_table_fk
FOREIGN KEY (created_by)
REFERENCES public.grant_applicant(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.spotlight_batch
ADD CONSTRAINT spotlight_batch_last_updated_by_to_grant_applicant_table_fk
FOREIGN KEY (last_updated_by)
REFERENCES public.grant_applicant(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

CREATE INDEX spotlight_batch_created_by_to_grant_applicant_table_idx ON spotlight_batch (created_by);
CREATE INDEX spotlight_batch_last_updated_by_to_grant_applicant_table_idx ON spotlight_batch (last_updated_by);

ALTER TABLE public.spotlight_batch_submission
ADD CONSTRAINT spotlight_submission_id_to_spotlight_submission_table_fk
FOREIGN KEY (spotlight_submission_id)
REFERENCES public.spotlight_submission(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.spotlight_batch_submission
ADD CONSTRAINT spotlight_batch_id_to_spotlight_batch_table_fk
FOREIGN KEY (spotlight_batch_id)
REFERENCES public.spotlight_batch(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

CREATE INDEX spotlight_batch_submission_spotlight_submission_id_to_spotlight_submission_table_idx ON spotlight_batch_submission (spotlight_submission_id);
CREATE INDEX spotlight_batch_spotlight_batch_id_to_spotlight_batch_table_idx ON spotlight_batch_submission (spotlight_batch_id);