CREATE UNIQUE INDEX grant_applicant_organisation_profile_applicant_id_idx ON grant_applicant_organisation_profile (applicant_id);
CREATE INDEX grant_submission_applicant_id_idx ON grant_submission (applicant_id);
CREATE INDEX grant_submission_application_id_idx ON grant_submission (application_id);