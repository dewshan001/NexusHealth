CREATE TABLE users (

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
CREATE TABLE sqlite_sequence(name,seq);
CREATE TABLE patients (

    id             INTEGER PRIMARY KEY AUTOINCREMENT,

    user_id        INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    patient_code   TEXT    UNIQUE,

    phone          TEXT,

    date_of_birth  DATE,

    gender         TEXT CHECK(gender IN ('male','female','other')),

    blood_type     TEXT,

    height         TEXT,

    weight         TEXT,

    heart_rate     TEXT,

    address        TEXT,

    last_visit     DATETIME,

    account_status TEXT NOT NULL DEFAULT 'active'

                   CHECK(account_status IN ('active','deactivated','archived'))

);
CREATE TABLE doctors (

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
CREATE TABLE appointments (

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
CREATE TABLE consultations (

    id             INTEGER PRIMARY KEY AUTOINCREMENT,

    appointment_id INTEGER NOT NULL UNIQUE REFERENCES appointments(id),

    doctor_id      INTEGER NOT NULL REFERENCES doctors(id),

    patient_id     INTEGER NOT NULL REFERENCES patients(id),

    diagnosis      TEXT,

    notes          TEXT,

    consulted_at   DATETIME DEFAULT CURRENT_TIMESTAMP

);
CREATE TABLE medicines (

    id          INTEGER PRIMARY KEY AUTOINCREMENT,

    name        TEXT    NOT NULL,

    category    TEXT,

    unit_price  REAL    NOT NULL DEFAULT 0.0,

    stock_level INTEGER NOT NULL DEFAULT 0,

    expiry_date DATE,

    status      TEXT    NOT NULL DEFAULT 'in_stock'

                CHECK(status IN ('in_stock','low_stock','out_of_stock','expired'))

);
CREATE TABLE prescriptions (

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
CREATE TABLE prescription_items (

    id              INTEGER PRIMARY KEY AUTOINCREMENT,

    prescription_id INTEGER NOT NULL REFERENCES prescriptions(id) ON DELETE CASCADE,

    medicine_id     INTEGER NOT NULL REFERENCES medicines(id),

    dosage          TEXT,

    frequency       TEXT,

    instructions    TEXT,

    quantity        INTEGER NOT NULL DEFAULT 1

);
CREATE TABLE invoices (

    id             INTEGER PRIMARY KEY AUTOINCREMENT,

    prescription_id INTEGER REFERENCES prescriptions(id),

    patient_id     INTEGER NOT NULL REFERENCES patients(id),

    appointment_id INTEGER REFERENCES appointments(id),

    total_amount   REAL    NOT NULL DEFAULT 0.0,

    discount       REAL             DEFAULT 0.0,

    amount_paid    REAL             DEFAULT 0.0,

    payment_status TEXT    NOT NULL DEFAULT 'unpaid'

                   CHECK(payment_status IN ('unpaid','partial','paid','waived')),

    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP

);
CREATE TABLE invoice_items (

    id          INTEGER PRIMARY KEY AUTOINCREMENT,

    invoice_id  INTEGER NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,

    medicine_id INTEGER NOT NULL REFERENCES medicines(id),

    medicine_name TEXT NOT NULL,

    quantity    INTEGER NOT NULL,

    unit_price  REAL    NOT NULL,

    line_total  REAL    NOT NULL,

    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP

);
CREATE TABLE transactions (

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
CREATE TABLE clinic_settings (

    id                    INTEGER PRIMARY KEY AUTOINCREMENT,

    clinic_name           TEXT DEFAULT 'NexusHealth Clinic',

    address               TEXT,

    phone                 TEXT,

    email                 TEXT,

    operating_hours_start TEXT DEFAULT '09:00',

    operating_hours_end   TEXT DEFAULT '17:00'

);
CREATE TABLE audit_logs (

    id           INTEGER PRIMARY KEY AUTOINCREMENT,

    user_id      INTEGER REFERENCES users(id),

    action       TEXT    NOT NULL,

    target_table TEXT,

    target_id    INTEGER,

    logged_at    DATETIME DEFAULT CURRENT_TIMESTAMP

);
CREATE INDEX idx_appt_date           ON appointments(appointment_date);
CREATE INDEX idx_appt_doctor         ON appointments(doctor_id);
CREATE INDEX idx_appt_patient        ON appointments(patient_id);
CREATE INDEX idx_rx_status           ON prescriptions(status);
CREATE INDEX idx_rx_patient          ON prescriptions(patient_id);
CREATE INDEX idx_med_status          ON medicines(status);
CREATE INDEX idx_txn_date            ON transactions(transacted_at);
CREATE INDEX idx_audit_user          ON audit_logs(user_id);
