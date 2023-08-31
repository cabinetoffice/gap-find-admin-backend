ALTER TABLE gap_user ADD CONSTRAINT unique_user_sub UNIQUE (user_sub);
ALTER TABLE grant_applicant ADD CONSTRAINT unique_user_id UNIQUE (user_id);
ALTER TABLE grant_admin ADD CONSTRAINT unique_user_id UNIQUE (user_id);