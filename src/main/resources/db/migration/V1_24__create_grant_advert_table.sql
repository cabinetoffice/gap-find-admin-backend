-- public.grant_advert definition
CREATE TABLE public.grant_advert (
	grant_advert_id uuid NOT NULL,
	contentful_entry_id varchar(255) NULL,
	contentful_slug varchar(255) NULL,
	created timestamp NOT NULL,
	grant_advert_name varchar(255) NULL,
	last_updated timestamp NOT NULL,
	response json NULL,
	status varchar(255) NOT NULL,
	"version" int4 NOT NULL,
	created_by int4 NOT NULL,
	last_updated_by int4 NOT NULL,
	scheme_id int4 NOT NULL,
	CONSTRAINT grant_advert_pkey PRIMARY KEY (grant_advert_id),
	CONSTRAINT uk_5gmiiic72gfpuoaghq7ukx7rq UNIQUE (contentful_slug),
	CONSTRAINT uk_o8chf7cuuc4il76408vatppov UNIQUE (contentful_entry_id)
);


-- public.grant_advert foreign keys

ALTER TABLE public.grant_advert ADD CONSTRAINT fkgkr2wfbxbhcehfofhkx9ewkdr FOREIGN KEY (scheme_id) REFERENCES public.grant_scheme(grant_scheme_id);
ALTER TABLE public.grant_advert ADD CONSTRAINT fkj37dbyjpgaof3c3l4k33qbsjt FOREIGN KEY (created_by) REFERENCES public.grant_admin(grant_admin_id);
ALTER TABLE public.grant_advert ADD CONSTRAINT fkkyx0kmnoeortbeg2uv6s1tjsl FOREIGN KEY (last_updated_by) REFERENCES public.grant_admin(grant_admin_id);