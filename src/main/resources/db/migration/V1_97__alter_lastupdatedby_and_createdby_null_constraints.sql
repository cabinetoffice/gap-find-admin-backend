ALTER TABLE IF EXISTS public.grant_advert
    ALTER COLUMN created_by DROP NOT NULL;

ALTER TABLE IF EXISTS public.grant_advert
    ALTER COLUMN last_updated_by DROP NOT NULL;

ALTER TABLE IF EXISTS public.grant_application
    ALTER COLUMN created_by DROP NOT NULL;
