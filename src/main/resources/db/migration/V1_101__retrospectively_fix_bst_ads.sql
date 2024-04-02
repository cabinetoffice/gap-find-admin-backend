UPDATE grant_advert
    SET opening_date = opening_date - INTERVAL '1 hour'
WHERE opening_date > '2024-03-31 01:00:00'::TIMESTAMP
    AND opening_date < '2024-10-27 02:00:00'::TIMESTAMP
    AND created > '2023-09-28 12:00:00'::TIMESTAMP
    AND status = 'SCHEDULED';

UPDATE grant_advert
    SET closing_date = closing_date - INTERVAL '1 hour'
WHERE closing_date > '2024-03-31 01:00:00'::TIMESTAMP
    AND closing_date < '2024-10-27 02:00:00'::TIMESTAMP
    AND created > '2023-09-28 12:00:00'::TIMESTAMP
    AND (status = 'PUBLISHED' OR status = 'SCHEDULED');