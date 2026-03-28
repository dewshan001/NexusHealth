-- =============================================
-- NexusHealth Clinical Management System
-- SQLite Database Schema — v3
-- (Updated: single specialization column in doctors table)
-- ======================================================

PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

-- ─────────────────────────────────────────────
-- 1. USERS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name       TEXT    NOT NULL,
    email           TEXT    NOT NULL UNIQUE,
    password_hash   TEXT    NOT NULL,
    role            TEXT    NOT NULL CHECK(role IN ('admin','doctor','receptionist','pharmacist','patient')),
    status          TEXT    NOT NULL DEFAULT 'active' CHECK(status IN ('active','inactive','deactivated','suspended')),
    profile_picture TEXT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 2. PATIENTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS patients (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    patient_code   TEXT    UNIQUE,
    phone          TEXT,
    date_of_birth  DATE,
    gender         TEXT CHECK(gender IN ('male','female','other')),
    blood_type     TEXT,
    address        TEXT,
    last_visit     DATETIME,
    account_status TEXT NOT NULL DEFAULT 'active'
                   CHECK(account_status IN ('active','deactivated','archived'))
);

-- ─────────────────────────────────────────────
-- 3. DOCTORS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS doctors (
    id                        INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id                   INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    license_number            TEXT,
    assigned_room             TEXT,
    specialization            TEXT,
    consultation_duration_min INTEGER DEFAULT 30,
    working_hours_start       TEXT    DEFAULT '09:00',
    working_hours_end         TEXT    DEFAULT '17:00',
    years_experience          INTEGER DEFAULT 0,
    rating                    REAL    DEFAULT 0.0
);

-- ─────────────────────────────────────────────
-- 4. APPOINTMENTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS appointments (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id        INTEGER NOT NULL REFERENCES patients(id),
    doctor_id         INTEGER NOT NULL REFERENCES doctors(id),
    booked_by         INTEGER REFERENCES users(id),
    appointment_date  DATE    NOT NULL,
    appointment_time  TEXT    NOT NULL,
    status            TEXT    NOT NULL DEFAULT 'scheduled'
                      CHECK(status IN ('scheduled','confirmed','completed','cancelled','no_show')),
    notes             TEXT,
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 5. CONSULTATIONS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS consultations (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_id INTEGER NOT NULL UNIQUE REFERENCES appointments(id),
    doctor_id      INTEGER NOT NULL REFERENCES doctors(id),
    patient_id     INTEGER NOT NULL REFERENCES patients(id),
    diagnosis      TEXT,
    notes          TEXT,
    consulted_at   DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 6. MEDICINES
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medicines (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    batch_number TEXT,
    category    TEXT,
    unit_price  REAL    NOT NULL DEFAULT 0.0,
    stock_level INTEGER NOT NULL DEFAULT 0,
    expiry_date DATE,
    status      TEXT    NOT NULL DEFAULT 'in_stock'
                CHECK(status IN ('in_stock','low_stock','out_of_stock','expired'))
);

-- ─────────────────────────────────────────────
-- 7. PRESCRIPTIONS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS prescriptions (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    consultation_id INTEGER NOT NULL REFERENCES consultations(id),
    doctor_id       INTEGER NOT NULL REFERENCES doctors(id),
    patient_id      INTEGER NOT NULL REFERENCES patients(id),
    status          TEXT    NOT NULL DEFAULT 'pending'
                    CHECK(status IN ('pending','dispensed','cancelled')),
    issued_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    dispensed_at    DATETIME,
    dispensed_by    INTEGER REFERENCES users(id)
);

-- ─────────────────────────────────────────────
-- 8. PRESCRIPTION_ITEMS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS prescription_items (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    prescription_id INTEGER NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,
    medicine_id     INTEGER NOT NULL REFERENCES medicines(id),
    dosage          TEXT,
    frequency       TEXT,
    instructions    TEXT,
    quantity        INTEGER NOT NULL DEFAULT 1
);

-- ─────────────────────────────────────────────
-- 9. INVOICES
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS invoices (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id     INTEGER NOT NULL REFERENCES patients(id),
    appointment_id INTEGER REFERENCES appointments(id),
    total_amount   REAL    NOT NULL DEFAULT 0.0,
    discount       REAL             DEFAULT 0.0,
    amount_paid    REAL             DEFAULT 0.0,
    payment_status TEXT    NOT NULL DEFAULT 'unpaid'
                   CHECK(payment_status IN ('unpaid','partial','paid','waived')),
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 10. TRANSACTIONS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS transactions (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_id       INTEGER NOT NULL REFERENCES invoices(id),
    transaction_code TEXT    UNIQUE,
    type             TEXT    NOT NULL,
    department       TEXT,
    amount           REAL    NOT NULL DEFAULT 0.0,
    status           TEXT    NOT NULL DEFAULT 'settled'
                     CHECK(status IN ('settled','pending','refunded')),
    transacted_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 11. CLINIC_SETTINGS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS clinic_settings (
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    clinic_name           TEXT DEFAULT 'NexusHealth Clinic',
    address               TEXT,
    phone                 TEXT,
    email                 TEXT,
    operating_hours_start TEXT DEFAULT '09:00',
    operating_hours_end   TEXT DEFAULT '17:00'
);

-- ─────────────────────────────────────────────
-- 12. AUDIT_LOGS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_logs (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER REFERENCES users(id),
    action       TEXT    NOT NULL,
    target_table TEXT,
    target_id    INTEGER,
    logged_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- 13. PHARMACIST_ALERTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pharmacist_alerts (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    medicine_id   INTEGER NOT NULL REFERENCES medicines(id) ON DELETE CASCADE,
    medicine_name TEXT    NOT NULL,
    alert_type    TEXT    NOT NULL CHECK(alert_type IN ('expiry_soon','low_stock')),
    severity      TEXT    NOT NULL CHECK(severity IN ('warning','urgent')),
    message       TEXT    NOT NULL,
    is_active     INTEGER NOT NULL DEFAULT 1,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at   DATETIME,
    UNIQUE(medicine_id, alert_type)
);

-- ─────────────────────────────────────────────
-- INDEXES
-- ─────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_appt_date           ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appt_doctor         ON appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appt_patient        ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_rx_status           ON prescriptions(status);
CREATE INDEX IF NOT EXISTS idx_rx_patient          ON prescriptions(patient_id);
CREATE INDEX IF NOT EXISTS idx_med_status          ON medicines(status);
CREATE INDEX IF NOT EXISTS idx_txn_date            ON transactions(transacted_at);
CREATE INDEX IF NOT EXISTS idx_audit_user          ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_ph_alert_active     ON pharmacist_alerts(is_active, severity, updated_at);

-- ─────────────────────────────────────────────
-- TRIGGER: auto-update updated_at on users
-- ─────────────────────────────────────────────
-- Note: SQLite trigger created via application layer to avoid parsing issues

-- ─────────────────────────────────────────────
-- SEED DATA
-- ─────────────────────────────────────────────
INSERT OR IGNORE INTO clinic_settings (id, clinic_name, address, phone, email)
VALUES (1, 'NexusHealth', '123 Wellness Ave, Suite 400', '(555) 123-4567', 'contact@nexushealth.com');

INSERT OR IGNORE INTO specializations (title, description) VALUES
    ('Cardiology',     'Heart and cardiovascular system'),
    ('Pediatrics',     'Medical care for infants and children'),
    ('Dermatology',    'Skin, hair and nail conditions'),
    ('General Practice','Primary and preventive care'),
    ('Neurology',      'Brain and nervous system disorders');
