ALTER TABLE grant_application
RENAME COLUMN version to revision;

ALTER TABLE grant_advert
RENAME COLUMN version to revision;

ALTER TABLE grant_scheme
ADD COLUMN revision INTEGER NOT NULL DEFAULT(1);