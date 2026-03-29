-- Add test users for all roles
-- Password for all: Password123! (BCrypt hash)
-- Use this to test all dashboard pages

-- Admin user (if not exists)
INSERT OR IGNORE INTO users (full_name, email, password_hash, role, status)
VALUES ('Admin User', 'admin@clinic.com', '$2a$10$SlVZQWFHRWd1OFRnRUN1duzVqRhFEkLQkQq5yvIa4A/gT0y9V6KGy', 'admin', 'active');

-- Doctor user
INSERT OR IGNORE INTO users (full_name, email, password_hash, role, status)
VALUES ('Dr. Sarah Johnson', 'doctor@clinic.com', '$2a$10$SlVZQWFHRWd1OFRnRUN1duzVqRhFEkLQkQq5yvIa4A/gT0y9V6KGy', 'doctor', 'active');

-- Patient user
INSERT OR IGNORE INTO users (full_name, email, password_hash, role, status)
VALUES ('John Doe', 'patient@clinic.com', '$2a$10$SlVZQWFHRWd1OFRnRUN1duzVqRhFEkLQkQq5yvIa4A/gT0y9V6KGy', 'patient', 'active');

-- Pharmacist user
INSERT OR IGNORE INTO users (full_name, email, password_hash, role, status)
VALUES ('Mike Pharmacy', 'pharmacist@clinic.com', '$2a$10$SlVZQWFHRWd1OFRnRUN1duzVqRhFEkLQkQq5yvIa4A/gT0y9V6KGy', 'pharmacist', 'active');

-- Receptionist user
INSERT OR IGNORE INTO users (full_name, email, password_hash, role, status)
VALUES ('Emily Reception', 'receptionist@clinic.com', '$2a$10$SlVZQWFHRWd1OFRnRUN1duzVqRhFEkLQkQq5yvIa4A/gT0y9V6KGy', 'receptionist', 'active');

-- Create doctor profile for the doctor user (get the user ID and use it)
-- First get the doctor user ID
INSERT OR IGNORE INTO doctors (user_id, license_number, phone, specialization, working_hours_start, working_hours_end, years_experience)
SELECT id, 'LN-00001', '555-0001', 'General Practice', '09:00', '17:00', 10
FROM users WHERE email = 'doctor@clinic.com' AND NOT EXISTS (
    SELECT 1 FROM doctors WHERE user_id = users.id
);

-- Create patient profile for the patient user
INSERT OR IGNORE INTO patients (user_id, patient_code, phone, date_of_birth, gender, blood_type, address, account_status)
SELECT id, 'PAT-00001', '555-0002', '1990-05-15', 'male', 'O+', '123 Main St', 'active'
FROM users WHERE email = 'patient@clinic.com' AND NOT EXISTS (
    SELECT 1 FROM patients WHERE user_id = users.id
);
