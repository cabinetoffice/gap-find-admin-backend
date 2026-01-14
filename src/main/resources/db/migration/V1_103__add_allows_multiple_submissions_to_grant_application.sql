ALTER TABLE grant_application
ADD COLUMN IF NOT EXISTS allows_multiple_submissions BOOLEAN DEFAULT false;

