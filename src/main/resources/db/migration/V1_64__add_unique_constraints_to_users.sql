ALTER TABLE gap_user ADD CONSTRAINT unique_user_sub UNIQUE (user_sub);
ALTER TABLE grant_applicant ADD CONSTRAINT unique_user_id UNIQUE (user_id);
ALTER TABLE grant_admin ADD CONSTRAINT unique_user_id UNIQUE (user_id);
ALTER TABLE grant_applicant_organisation_profile ADD CONSTRAINT unique_applicant_id UNIQUE (applicant_id);