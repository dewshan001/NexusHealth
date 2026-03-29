-- Insert test data for Reports & Analytics demonstration

-- 1. Create test patients and appointments (to get patients_visited count)
INSERT OR IGNORE INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, created_at)
VALUES 
  (1, 1, date('now'), '09:00', 'completed', datetime('now')),
  (1, 1, date('now'), '10:30', 'completed', datetime('now')),
  (2, 1, date('now'), '11:00', 'confirmed', datetime('now')),
  (3, 2, date('now'), '14:00', 'completed', datetime('now'));

-- 2. Create test invoices with pharmacy addons (for pharmacy sales)
INSERT OR IGNORE INTO invoices (invoice_number, patient_id, doctor_id, patient_name, consultation_type, consultation_amount, pharmacy_addons, subtotal, discount_amount, total_amount, status, created_at, paid_at)
VALUES 
  ('INV-001', 1, 1, 'John Doe', 'Consultation', 500.00, 1200.00, 1700.00, 100.00, 1600.00, 'paid', datetime('now'), datetime('now')),
  ('INV-002', 2, 1, 'Jane Smith', 'Consultation', 500.00, 645.50, 1145.50, 50.00, 1095.50, 'paid', datetime('now'), datetime('now')),
  ('INV-003', 3, 2, 'Robert Johnson', 'Consultation', 750.00, 0.00, 750.00, 0.00, 750.00, 'paid', datetime('now'), datetime('now'));

-- 3. Create test transactions (for daily revenue)
INSERT OR IGNORE INTO transactions (invoice_id, transaction_code, type, department, amount, status, transacted_at)
VALUES 
  (1, 'TXN-001001', 'Payment', 'Cardiology', 1600.00, 'settled', datetime('now')),
  (2, 'TXN-001002', 'Payment', 'General Practice', 1095.50, 'settled', datetime('now')),
  (3, 'TXN-001003', 'Consultation Fee', 'Neurology', 750.00, 'settled', datetime('now')),
  (1, 'TXN-001004', 'Pharmacy Purchase', 'Dispensary', 500.00, 'settled', datetime('now', '-1 days')),
  (2, 'TXN-001005', 'Lab Test', 'Pathology', 250.00, 'settled', datetime('now', '-2 days'));
