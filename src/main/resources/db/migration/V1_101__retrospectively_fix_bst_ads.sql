UPDATE grant_advert
    SET opening_date = opening_date AT TIME ZONE 'Europe/London'
WHERE created > '2023-09-28 12:00:00'::TIMESTAMP
	AND status = 'SCHEDULED';

UPDATE grant_advert
    SET closing_date = closing_date AT TIME ZONE 'Europe/London'
WHERE created > '2023-09-28 12:00:00'::TIMESTAMP
	AND (status = 'PUBLISHED' OR status = 'SCHEDULED');