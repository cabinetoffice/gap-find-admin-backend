
-- Add new date fields

ALTER TABLE grant_advert
ADD COLUMN opening_date timestamp,
ADD COLUMN closing_date timestamp,
ADD COLUMN first_published_date timestamp,
ADD COLUMN last_published_date timestamp,
ADD COLUMN unpublished_date timestamp;