CREATE TYPE grant_mandatory_question_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE grant_mandatory_question_type AS ENUM ('Limited company', 'Non-limited company','Registered charity', 'Unregistered charity', 'Other');



-- grant_mandatory_questions
CREATE TABLE IF NOT EXISTS public.grant_mandatory_questions (
	id uuid NOT NULL,
	grant_scheme_id int4 NOT NULL,
    submission_id uuid NULL,
	name varchar(255) NULL,
	address_line_1 varchar(255) NULL,
	address_line_2 varchar(255) NULL,
	city varchar(255) NULL,
	county varchar(255) NULL,
	postcode varchar(255) NULL,
	org_type grant_mandatory_question_type NULL,
	companies_house_number varchar(255) NULL,
	charity_commission_number varchar(255) NULL,
	funding_amount numeric(16,2) NULL,
	funding_location varchar(255) NULL,
    status grant_mandatory_question_status NOT NULL,
    version int4 NOT NULL,
	created timestamp NOT NULL,
	created_by BIGINT NOT NULL,
    last_updated timestamp ,
	last_updated_by BIGINT,
	CONSTRAINT grant_mandatory_questions_pkey PRIMARY KEY (id)
);


-- grant_mandatory_questions foreign keys

ALTER TABLE public.grant_mandatory_questions
ADD CONSTRAINT grant_mandatory_questions_grant_scheme_id_to_grant_scheme_table_fk
FOREIGN KEY (grant_scheme_id)
REFERENCES public.grant_scheme(grant_scheme_id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.grant_mandatory_questions
ADD CONSTRAINT grant_mandatory_questions_grant_scheme_id_to_grant_submission_table_fk
FOREIGN KEY (submission_id)
REFERENCES public.grant_submission(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.grant_mandatory_questions
ADD CONSTRAINT grant_mandatory_questions_created_by_to_grant_applicant_table_fk
FOREIGN KEY (created_by)
REFERENCES public.grant_applicant(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

ALTER TABLE public.grant_mandatory_questions
ADD CONSTRAINT grant_mandatory_questions_last_updated_by_to_grant_applicant_table_fk
FOREIGN KEY (last_updated_by)
REFERENCES public.grant_applicant(id)
MATCH simple
ON DELETE CASCADE
ON UPDATE NO ACTION;

-- create index to foreign keys

CREATE INDEX grant_mandatory_questions_grant_scheme_id_to_grant_scheme_table_idx ON grant_mandatory_questions (grant_scheme_id);
CREATE INDEX grant_mandatory_questions_grant_scheme_id_to_grant_submission_table_idx ON grant_mandatory_questions (submission_id);
CREATE INDEX grant_mandatory_questions_created_by_to_grant_applicant_table_idx ON grant_mandatory_questions (created_by);
CREATE INDEX grant_mandatory_questions_last_updated_by_to_grant_applicant_table_idx ON grant_mandatory_questions (last_updated_by);