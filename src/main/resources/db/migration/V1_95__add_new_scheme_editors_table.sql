-- Table: public.scheme_editors

DROP TABLE IF EXISTS public.scheme_editors;

CREATE TABLE scheme_editors (
	grant_admin_id integer NOT NULL,
    grant_scheme_id integer NOT NULL,
	CONSTRAINT admin FOREIGN KEY (grant_admin_id) REFERENCES grant_admin(grant_admin_id) ON DELETE CASCADE,
	CONSTRAINT scheme FOREIGN KEY (grant_scheme_id) REFERENCES grant_scheme(grant_scheme_id) ON DELETE CASCADE
);

ALTER TABLE scheme_editors ADD CONSTRAINT scheme_editors_pk PRIMARY KEY (grant_admin_id, grant_scheme_id);
CREATE UNIQUE INDEX scheme_editors_index ON scheme_editors(grant_admin_id, grant_scheme_id);

-- Migrating all scheme_owners to scheme_permissions as editors
INSERT INTO scheme_editors (grant_admin_id, grant_scheme_id)
    SELECT created_by, grant_scheme_id
    FROM grant_scheme;
