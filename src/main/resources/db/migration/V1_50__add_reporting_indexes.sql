create index concurrently if not exists idx_grant_applicant_org on grant_applicant_organisation_profile using btree(applicant_id);
create index concurrently if not exists idx_grant_submission_applicant_id on grant_submission using btree(applicant_id);
