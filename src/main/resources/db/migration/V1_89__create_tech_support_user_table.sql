CREATE TABLE IF NOT EXISTS tech_support_user(
    id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
       funder_id integer,
       user_sub character varying(128) NOT NULL,
       CONSTRAINT tech_support_user_pkey PRIMARY KEY (id),
       CONSTRAINT fk_funding_org_tech_support_user FOREIGN KEY (funder_id)
           REFERENCES public.grant_funding_organisation (funder_id) MATCH SIMPLE
           ON UPDATE NO ACTION
           ON DELETE NO ACTION
);