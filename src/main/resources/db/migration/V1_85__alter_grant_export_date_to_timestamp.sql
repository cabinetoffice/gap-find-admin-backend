ALTER TABLE public.grant_export
ALTER COLUMN created
TYPE TIMESTAMP;

ALTER TABLE public.grant_export
ALTER COLUMN last_updated
TYPE TIMESTAMP;