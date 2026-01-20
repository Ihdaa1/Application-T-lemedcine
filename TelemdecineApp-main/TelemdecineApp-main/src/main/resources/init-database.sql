# Database Setup SQL Script

-- Create database
CREATE DATABASE IF NOT EXISTS telemedicine_db;

-- Connect to database
\c telemedicine_db;

-- Note: Tables will be created automatically by Hibernate with spring.jpa.hibernate.ddl-auto=update

-- Optional: Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_prescriptions_patient_id ON prescriptions(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_doctor_id ON prescriptions(doctor_id);
CREATE INDEX IF NOT EXISTS idx_medical_records_patient_id ON medical_records(patient_id);

-- Optional: Insert initial admin user (password: admin123)
-- Password is hashed using BCrypt
INSERT INTO users (email, password, first_name, last_name, role, active, created_at, updated_at)
VALUES ('admin@telemedicine.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Rogg.odCp.mTa0.Xy1.FcX6m', 
        'Admin', 'User', 'ADMIN', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Sample doctors insert (password: doctor123)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, active, created_at, updated_at)
VALUES 
    ('dr.smith@telemedicine.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Rogg.odCp.mTa0.Xy1.FcX6m', 
     'John', 'Smith', '+1234567890', 'DOCTOR', true, NOW(), NOW()),
    ('dr.johnson@telemedicine.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Rogg.odCp.mTa0.Xy1.FcX6m', 
     'Sarah', 'Johnson', '+1234567891', 'DOCTOR', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Insert doctor profiles
INSERT INTO doctors (user_id, specialization, license_number, years_of_experience, 
                     consultation_fee, available_for_consultation, created_at, updated_at)
SELECT u.id, 'Cardiology', 'LIC001', 15, 100.00, true, NOW(), NOW()
FROM users u WHERE u.email = 'dr.smith@telemedicine.com'
ON CONFLICT (license_number) DO NOTHING;

INSERT INTO doctors (user_id, specialization, license_number, years_of_experience, 
                     consultation_fee, available_for_consultation, created_at, updated_at)
SELECT u.id, 'Dermatology', 'LIC002', 10, 80.00, true, NOW(), NOW()
FROM users u WHERE u.email = 'dr.johnson@telemedicine.com'
ON CONFLICT (license_number) DO NOTHING;
