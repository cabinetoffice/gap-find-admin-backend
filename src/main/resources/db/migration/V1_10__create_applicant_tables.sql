CREATE TABLE IF NOT EXISTS diligence_check
(
    id uuid NOT NULL,
    address_county character varying(250),
    address_postcode character varying(8),
    address_street character varying(250),
    address_town character varying(250),
    application_amount character varying(255),
    application_number character varying(255),
    charity_number character varying(15),
    check_type integer,
    companies_house_number character varying(8),
    created timestamp without time zone,
    organisation_name character varying(250),
    submission_id uuid,
    CONSTRAINT diligence_check_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE grant_applicant_id_seq
INCREMENT 1
START 1;

CREATE TABLE IF NOT EXISTS grant_applicant
(
    id bigint NOT NULL DEFAULT nextval('grant_applicant_id_seq'::regclass),
    user_id uuid,
    CONSTRAINT grant_applicant_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE grant_applicant_organisation_profile_id_seq
INCREMENT 1
START 1;

CREATE TABLE IF NOT EXISTS grant_applicant_organisation_profile
(
    id bigint NOT NULL DEFAULT nextval('grant_applicant_organisation_profile_id_seq'::regclass),
    address_line1 character varying(255),
    address_line2 character varying(255),
    charity_commission_number character varying(255),
    companies_house_number character varying(255),
    county character varying(255),
    legal_name character varying(255),
    postcode character varying(255),
    town character varying(255),
    type character varying(255),
    applicant_id bigint,
    CONSTRAINT grant_applicant_organisation_profile_pkey PRIMARY KEY (id),
    CONSTRAINT grant_applicant_organisation_profile_applicant_id_fkey FOREIGN KEY (applicant_id)
        REFERENCES grant_applicant (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);


CREATE TABLE IF NOT EXISTS grant_beneficiary
(
    id uuid NOT NULL,
    application_id integer,
    created timestamp without time zone,
    created_by bigint,
    scheme_id integer,
    submission_id uuid,
    CONSTRAINT grant_beneficiary_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS grant_submission
(
    id uuid NOT NULL,
    application_name character varying(255),
    created timestamp without time zone,
    definition json,
    last_updated timestamp without time zone,
    status character varying(255),
    submitted_date timestamp without time zone,
    version integer,
    applicant_id bigint,
    application_id integer,
    created_by bigint,
    last_updated_by bigint,
    scheme_id integer,
    CONSTRAINT submission_pkey PRIMARY KEY (id),
    CONSTRAINT submission_created_by_fkey FOREIGN KEY (created_by)
        REFERENCES grant_applicant (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT submission_applicant_id_fkey FOREIGN KEY (applicant_id)
        REFERENCES grant_applicant (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT submission_last_updated_by_fkey FOREIGN KEY (last_updated_by)
        REFERENCES grant_applicant (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT submission_last_updated_by_admin_fkey FOREIGN KEY (last_updated_by)
        REFERENCES grant_admin (grant_admin_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT submission_scheme_id_fkey FOREIGN KEY (scheme_id)
        REFERENCES grant_scheme (grant_scheme_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT submission_application_id_fkey FOREIGN KEY (application_id)
        REFERENCES grant_application (grant_application_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
