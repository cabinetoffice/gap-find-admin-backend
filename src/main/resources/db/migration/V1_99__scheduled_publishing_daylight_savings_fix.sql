-- Replace view for scheduler

DROP VIEW IF EXISTS ADVERT_SCHEDULER_VIEW;
CREATE VIEW ADVERT_SCHEDULER_VIEW AS
SELECT GRANT_ADVERT_ID AS ID,
    CASE
        WHEN
            STATUS = 'SCHEDULED'
            AND (OPENING_DATE AT TIME ZONE 'Europe/London') <= NOW() AT TIME ZONE 'Europe/London'
        THEN 'PUBLISH'
        WHEN
            STATUS = 'PUBLISHED'
            AND (CLOSING_DATE AT TIME ZONE 'Europe/London') <= NOW() AT TIME ZONE 'Europe/London'
        THEN 'UNPUBLISH'
    END AS ACTION
FROM GRANT_ADVERT
WHERE (
    STATUS = 'SCHEDULED' AND (OPENING_DATE AT TIME ZONE 'Europe/London') <= NOW() AT TIME ZONE 'Europe/London'
) OR (
    STATUS = 'PUBLISHED' AND (CLOSING_DATE AT TIME ZONE 'Europe/London') <= (NOW() AT TIME ZONE 'Europe/London')
);