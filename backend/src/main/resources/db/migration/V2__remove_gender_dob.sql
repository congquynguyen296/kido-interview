-- V2: Remove gender and date_of_birth columns from users table
-- These fields are no longer needed in the User entity.
ALTER TABLE users DROP COLUMN IF EXISTS gender;
ALTER TABLE users DROP COLUMN IF EXISTS date_of_birth;
