ALTER TABLE grant_submission ADD last_required_checks_export timestamp without time zone;

ALTER TABLE grant_application DROP spotlight_export;