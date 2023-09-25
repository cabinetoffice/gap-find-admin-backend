-- Replace view for scheduler

DROP VIEW IF EXISTS ADVERT_SCHEDULER_VIEW;

ALTER TABLE grant_advert ALTER COLUMN opening_date TYPE timestamp with time zone;
ALTER TABLE grant_advert ALTER COLUMN closing_date TYPE timestamp with time zone;

CREATE VIEW ADVERT_SCHEDULER_VIEW AS
SELECT GRANT_ADVERT_ID AS ID,
    CASE
        WHEN
            STATUS = 'SCHEDULED'
            AND (OPENING_DATE AT TIME ZONE 'Europe/London')::date <= (NOW() AT TIME ZONE 'Europe/London')::date
        THEN 'PUBLISH'
        WHEN
            STATUS = 'PUBLISHED'
            AND (CLOSING_DATE AT TIME ZONE 'Europe/London')::date <= ((NOW() AT TIME ZONE 'Europe/London')::date - interval '1' DAY)
        THEN 'UNPUBLISH'
    END AS ACTION
FROM GRANT_ADVERT
WHERE (
    STATUS = 'SCHEDULED' AND (OPENING_DATE AT TIME ZONE 'Europe/London')::date <= (NOW() AT TIME ZONE 'Europe/London')::date
) OR (
    STATUS = 'PUBLISHED' AND (CLOSING_DATE AT TIME ZONE 'Europe/London')::date <= ((NOW() AT TIME ZONE 'Europe/London')::date - interval '1' DAY)
);