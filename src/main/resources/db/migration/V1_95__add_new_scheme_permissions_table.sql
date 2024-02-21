-- Table: public.scheme_permissions

DROP TABLE IF EXISTS public.scheme_permissions;

CREATE TABLE scheme_permissions (
	grant_admin_id integer NOT NULL,
    grant_scheme_id integer NOT NULL,
	CONSTRAINT admin FOREIGN KEY (grant_admin_id) REFERENCES grant_admin(grant_admin_id) ON DELETE CASCADE,
	CONSTRAINT scheme FOREIGN KEY (grant_scheme_id) REFERENCES grant_scheme(grant_scheme_id) ON DELETE CASCADE
);

ALTER TABLE scheme_permissions ADD CONSTRAINT scheme_permissions_pk PRIMARY KEY (grant_admin_id, grant_scheme_id);
CREATE UNIQUE INDEX scheme_permissions_index ON scheme_permissions(grant_admin_id, grant_scheme_id);

-- Migrating all scheme_owners to scheme_permissions as editors
INSERT INTO scheme_permissions (grant_admin_id, grant_scheme_id)
    SELECT created_by, grant_scheme_id
    FROM grant_scheme;
