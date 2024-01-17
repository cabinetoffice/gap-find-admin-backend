ALTER TABLE grant_submission ALTER COLUMN mandatory_sections_completed DROP DEFAULT;
UPDATE grant_submission SET mandatory_sections_completed = null;