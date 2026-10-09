-- SQL to insert the process record for Journal Confirmation
-- Run this in your database before using the confirmation feature

-- Insert into process table
INSERT INTO dbadmin.process (pro_code, pro_desc) 
VALUES ('9.02', 'Confirm Editing of Journals');

-- Verify the insert
SELECT * FROM dbadmin.process WHERE pro_desc = 'Confirm Editing of Journals';
