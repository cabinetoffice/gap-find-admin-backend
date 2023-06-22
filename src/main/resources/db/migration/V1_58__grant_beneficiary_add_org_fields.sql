
-- Add new date fields

ALTER TABLE grant_beneficiary
ADD COLUMN organisation_group1 boolean NULL,
ADD COLUMN organisation_group2 boolean NULL,
ADD COLUMN organisation_group3 boolean NULL;