-- drop the created by and last updated by columns from spotlight submission table
ALTER TABLE spotlight_submission
DROP COLUMN created_by; 

ALTER TABLE spotlight_submission
DROP COLUMN last_updated_by;


-- drop the created by and last updated by columns from spotlight batch table
ALTER TABLE spotlight_batch
DROP COLUMN created_by; 

ALTER TABLE spotlight_batch
DROP COLUMN last_updated_by; 