ALTER TABLE feedback
ADD COLUMN created_by INTEGER;

UPDATE feedback SET created_by = 1;

ALTER TABLE feedback
ALTER COLUMN created_by SET NOT NULL;