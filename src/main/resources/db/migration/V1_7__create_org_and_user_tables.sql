CREATE TABLE gap_user
(
    gap_user_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    user_sub uuid,
    CONSTRAINT gap_user_pkey PRIMARY KEY (gap_user_id)
);

CREATE TABLE grant_funding_organisation
(
    funder_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    organisation_name character varying(250) NOT NULL,
    CONSTRAINT grant_funding_organisation_pkey PRIMARY KEY (funder_id)
);

CREATE TABLE grant_admin
(
    grant_admin_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    funder_id integer,
    user_id integer,
    CONSTRAINT grant_admin_pkey PRIMARY KEY (grant_admin_id),
    CONSTRAINT fk_gap_user_grant_admin FOREIGN KEY (user_id)
        REFERENCES public.gap_user (gap_user_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_funding_org_grant_admin FOREIGN KEY (funder_id)
        REFERENCES public.grant_funding_organisation (funder_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
