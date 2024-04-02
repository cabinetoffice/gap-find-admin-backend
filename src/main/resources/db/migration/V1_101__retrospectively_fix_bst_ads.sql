UPDATE grant_advert
    SET opening_date = opening_date - INTERVAL '1 hour'
WHERE to_char(opening_date, 'TZ') = 'BST'
    AND created > '2023-09-28 12:00:00'::TIMESTAMP
	AND status = 'SCHEDULED';

UPDATE grant_advert
    SET closing_date = closing_date - INTERVAL '1 hour'
WHERE to_char(closing_date, 'TZ') = 'BST'
    AND created > '2023-09-28 12:00:00'::TIMESTAMP
	AND status = 'PUBLISHED';