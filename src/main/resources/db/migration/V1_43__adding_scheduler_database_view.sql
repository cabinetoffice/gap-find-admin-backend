
-- Create index for scheduler fields

CREATE INDEX grant_adverts_schduler_dates ON grant_advert(status, opening_date, closing_date);

-- Add new view for scheduler

CREATE VIEW ADVERT_SCHEDULER_VIEW AS
SELECT GRANT_ADVERT_ID AS ID,
    CASE
        WHEN 
            STATUS = 'SCHEDULED'
            AND (OPENING_DATE AT TIME ZONE 'Europe/London')::date = (NOW() AT TIME ZONE 'Europe/London')::date 
        THEN 'PUBLISH'
        WHEN 
            STATUS = 'PUBLISHED'
            AND (CLOSING_DATE AT TIME ZONE 'Europe/London')::date = ((NOW() AT TIME ZONE 'Europe/London')::date - interval '1' DAY) 
        THEN 'UNPUBLISH'
    END AS ACTION
FROM GRANT_ADVERT
WHERE (
    STATUS = 'SCHEDULED' AND (OPENING_DATE AT TIME ZONE 'Europe/London')::date = (NOW() AT TIME ZONE 'Europe/London')::date
) OR (
    STATUS = 'PUBLISHED' AND (CLOSING_DATE AT TIME ZONE 'Europe/London')::date = ((NOW() AT TIME ZONE 'Europe/London')::date - interval '1' DAY)
);
