# Appointment Booking Interface - Development Prompt (Final Branch)

## Project Overview
Build an interactive appointment booking system for NexusHealth - a Clinical Management System with Java/Spring Boot backend and SQLite database. The system allows patients to search for doctors, view availability, and book in-person appointments through an intuitive calendar-based interface.

---

## Current Project Architecture

### Backend
- **Framework:** Spring Boot 4.0.5
- **Database:** SQLite with WAL mode
- **Security:** Spring Security for authentication
- **Templates:** Thymeleaf
- **Java Version:** 21

### Database Existing Tables
- `users` - All user accounts (admin, doctor, receptionist, pharmacist, patient)
- `doctors` - Doctor profiles with specialization, working hours, consultation duration
- `patients` - Patient profiles with medical data
- `appointments` - Appointment records
- `consultations` - Doctor consultation details
- `prescriptions` - Prescription management
- `medicines` - Pharmacy inventory
- `invoices` - Payment tracking
- `clinic_settings` - System configuration

### Frontend
- HTML5/CSS3/JavaScript with Material Design Icons
- Responsive layout (Desktop, Tablet, Mobile)
- Thymeleaf templates in `src/main/resources/templates/`
- Static files in `src/main/resources/static/`

---

## Feature Requirements

### 1. **Doctor Search & Filtering**
Integrate with existing `doctors` table:
- Search by:
  - Name (from users table)
  - Specialization (single column in doctors table)
  - Rating (doctors.rating)
  - Availability status
- Display doctor cards with:
  - Profile picture (from users.profile_picture)
  - Name, specialization, years of experience
  - Rating (doctors.rating)
  - Consultation fee (from clinic_settings.appointment_fee)
  - Working hours (doctors.working_hours_start/end)
  - Availability status
  - "Book Appointment" button

### 2. **Interactive Calendar Interface**
Fetch available slots based on:
- Doctor's `working_hours_start` and `working_hours_end`
- Doctor's `consultation_duration_min` (default: 30 minutes)
- Existing appointments from `appointments` table
- Features:
  - Month view with date grid
  - Highlight available dates in green (#31C48D)
  - Gray out unavailable dates
  - Navigation for previous/next month
  - Show available slot count per date
  - Disable past dates

### 3. **Time Slot Selection**
- Display 30-min slots for selected date based on:
  - Doctor's working hours
  - Consultation duration
  - Booked appointments (check appointments table)
- Color coding:
  - Green: Available (#31C48D)
  - Red/Gray: Booked (#E5354B)
  - Yellow: Limited availability (1-2 slots)
- Show slot details:
  - Time in HH:MM format
  - Doctor name
  - Slot status

### 4. **Booking Form**
Collect patient information before confirmation:
- **Auto-filled from session (if logged in):**
  - Patient full name (from users.full_name)
  - Email (from users.email)
  - Phone (from users.phone or patients.phone)

- **Additional Fields:**
  - Symptoms/Chief complaint (TEXT) - optional
  - Medical history (from patients table - blood_type, height, weight, etc.)
  - Preferred communication language (ENUM: English, Spanish, etc.)
  - Emergency contact name & phone (optional)
  - Consent checkbox for privacy policy (required)

### 5. **Booking Confirmation**
- Display summary with:
  - Doctor name & specialization
  - Appointment date & time
  - Clinic location (from clinic_settings.address)
  - Consultation fee (from clinic_settings.appointment_fee)
  - Patient name & phone
  - Duration (from doctors.consultation_duration_min)
  
- Actions:
  - Confirm booking → INSERT into `appointments` table
  - Edit details → Return to form
  - Cancel → Exit booking flow

- Success message with:
  - Booking confirmation number (appointment.id)
  - Download receipt/appointment slip
  - Option to add to calendar (Google/Outlook)

### 6. **Database Integration**
**INSERT Statement for Appointment:**
```sql
INSERT INTO appointments (patient_id, doctor_id, booked_by, appointment_date, appointment_time, status, notes, created_at)
VALUES (?, ?, ?, ?, ?, 'scheduled', ?, CURRENT_TIMESTAMP);
```

**Key Relationships:**
- Patient must exist in `patients` table (linked to users)
- Doctor must exist in `doctors` table (linked to users)
- Appointment status: 'scheduled' (can be changed to 'confirmed', 'completed', 'cancelled', 'no_show')

### 7. **User Interface/UX**
- **Responsive Design:**
  - Desktop (1200px+): Full calendar with sidebar doctor list
  - Tablet (768px-1199px): Adjusted layout
  - Mobile (below 768px): Stack layout, carousel for slots

- **Visual Elements:**
  - Use DM Sans font (already in project)
  - Primary color: #0066FF
  - Success: #31C48D (green)
  - Error: #E5354B (red)
  - Background: #F9FAFB

- **User Feedback:**
  - Loading spinners during data fetch
  - Error messages for invalid selections
  - Toast notifications for actions
  - Hover effects on interactive elements
  - ARIA labels for accessibility

### 8. **Workflow Steps**
```
1. Doctor Selection (Search & Filter)
   ↓
2. View Doctor Profile & Available Dates
   ↓
3. Select Date from Interactive Calendar
   ↓
4. Select Time Slot from Available Options
   ↓
5. Fill Patient Information Form
   ↓
6. Review Booking Summary
   ↓
7. Confirm Appointment (INSERT to DB)
   ↓
8. View Confirmation & Download Slip
```

---

## Technical Implementation

### Backend Requirements

#### Spring Boot Controller Endpoints
```java
// GET list all available doctors with filtering
GET /api/doctors?specialization=Cardiology&search=John

// GET doctor details with availability
GET /api/doctors/{doctorId}

// GET available time slots for a date range
GET /api/doctors/{doctorId}/availability?startDate=2026-04-24&endDate=2026-05-24

// POST create new appointment
POST /api/appointments
{
  "doctorId": 1,
  "patientId": 123,
  "appointmentDate": "2026-04-24",
  "appointmentTime": "09:00",
  "notes": "Patient symptoms"
}

// GET appointment confirmation
GET /api/appointments/{appointmentId}

// PUT update appointment
PUT /api/appointments/{appointmentId}

// DELETE cancel appointment
DELETE /api/appointments/{appointmentId}

// GET clinic settings
GET /api/clinic-settings
```

#### Service Layer Logic Required
```java
// DoctorService
- List<Doctor> searchDoctors(String specialization, String search)
- Doctor getDoctorById(Long id)
- List<DoctorAvailability> getAvailableSlots(Long doctorId, LocalDate date)

// AppointmentService
- Appointment createAppointment(AppointmentRequest request)
- Appointment getAppointmentById(Long id)
- void cancelAppointment(Long id)
- boolean isSlotAvailable(Long doctorId, LocalDate date, LocalTime time)
```

#### Database Queries Needed
```sql
-- Get all doctors with working hours
SELECT d.id, d.user_id, d.specialization, d.working_hours_start, 
       d.working_hours_end, d.consultation_duration_min, d.rating,
       d.years_experience, u.full_name, u.profile_picture, u.phone
FROM doctors d
JOIN users u ON d.user_id = u.id
WHERE d.specialization LIKE ? AND u.full_name LIKE ?
ORDER BY d.rating DESC;

-- Get booked appointments for a doctor on a specific date
SELECT appointment_date, appointment_time, consultation_duration_min
FROM appointments a
JOIN doctors d ON a.doctor_id = d.id
WHERE a.doctor_id = ? AND a.appointment_date = ? AND a.status != 'cancelled';

-- Insert new appointment
INSERT INTO appointments (patient_id, doctor_id, booked_by, appointment_date, appointment_time, status, notes)
VALUES (?, ?, ?, ?, ?, 'scheduled', ?);
```

### Frontend Stack
- **HTML5** - Structure
- **CSS3** - Grid/Flexbox responsive layout
- **JavaScript (Vanilla ES6+)** - Interactivity
  - Fetch API for backend communication
  - Day.js for date manipulation (or Flatpickr for calendar)
  - LocalStorage for form draft saving

### Files to Create/Update

```
Frontend (Thymeleaf templates):
src/main/resources/templates/
├── appointment-booking.html         # Main booking page

Backend (Java/Spring Boot):
src/main/java/com/NexusHealth/
├── controller/
│   ├── AppointmentController.java
│   └── DoctorController.java
├── service/
│   ├── AppointmentService.java
│   └── DoctorService.java
├── model/
│   ├── Doctor.java
│   ├── Appointment.java
│   └── DoctorAvailability.java
├── repository/
│   ├── AppointmentRepository.java
│   └── DoctorRepository.java
└── util/
    └── SlotAvailabilityCalculator.java

Static assets:
src/main/resources/static/
├── css/
│   └── appointment-booking.css
├── js/
│   ├── appointment-booking.js
│   ├── calendar.js
│   ├── doctor-search.js
│   ├── slot-selector.js
│   └── booking-form.js
└── images/
    └── default-doctor.png
```

---

## Implementation Phases

### Phase 1 (MVP) - Core Booking
- [ ] Create AppointmentController with basic endpoints
- [ ] Implement DoctorService to fetch doctors from DB
- [ ] Build appointment-booking.html template
- [ ] Create doctor search & filter UI
- [ ] Implement basic calendar view
- [ ] Build time slot selection
- [ ] Create booking form
- [ ] INSERT appointment to database
- [ ] Display confirmation page

### Phase 2 - Enhancement
- [ ] Dynamic availability calculation from database
- [ ] Doctor availability search optimization
- [ ] Appointment history for logged-in patients
- [ ] Email receipt generation (optional)
- [ ] Print appointment slip functionality
- [ ] Cancel/Reschedule appointment UI

### Phase 3 - Advanced Features
- [ ] Payment integration (if needed)
- [ ] Advanced doctor filtering (ratings, experience)
- [ ] Recurring appointment booking
- [ ] Appointment reminder system
- [ ] Analytics dashboard for appointments

---

## Key Database Constraints

**From schema.sql analysis:**

1. **Appointment Status:**
   - 'scheduled' → Default when booked
   - 'confirmed' → After patient confirmation
   - 'completed' → After consultation
   - 'cancelled' → User cancellation
   - 'no_show' → Patient didn't show

2. **Doctor Working Hours:**
   - Default: 09:00 - 17:00
   - Customizable per doctor
   - Consultation duration: Default 30 minutes (adjustable)

3. **Patient Data Validation:**
   - Must have user_id (from users table)
   - Optional: phone, date_of_birth, gender, blood_type, medical history

4. **Clinic Settings:**
   - Default appointment fee: 500.0 (currency unit)
   - Default hours: 09:00 - 17:00
   - Location: stored in clinic_settings table

---

## Validation Rules

**Form Validation:**
- Patient name: Required, min 3 characters
- Phone: Required, valid format (10 digits)
- Email: Required, valid format
- Date: Must be future date, doctor available
- Time: Within doctor's working hours
- Consent checkbox: Must be checked

**Business Rules:**
- Cannot book past appointments
- Cannot book beyond 2 months in advance
- No double-booking (same patient, same time)
- Minimum 24 hours for cancellation
- Doctor cannot have conflicting appointments

---

## Design Guidelines

**Color Scheme:**
- Primary: #0066FF (Medical Blue)
- Success: #31C48D (Green - available)
- Error: #E5354B (Red - unavailable)
- Background: #F9FAFB
- Text: #111827 (dark), #6B7280 (muted)

**Typography:**
- Font: DM Sans (already configured in project)
- Headings: Bold/Semibold
- Body: Regular weight

**Spacing:**
- Padding: 1rem, 1.5rem, 2rem
- Margin: 1rem, 1.5rem, 2rem
- Gap: 0.5rem, 1rem, 1.5rem

---

## Error Handling

Handle these scenarios:
- Doctor profile not found in database
- No availability for selected date
- Appointment already booked (race condition)
- Invalid form submission
- Database connection errors
- Authorization errors (patient must be logged in)
- Clinic closed on date (clinic_settings)

---

## Success Metrics

- Book appointment in < 5 minutes
- Mobile booking completion rate > 80%
- Zero double-bookings
- Confirmation display on first try
- Database transaction success rate > 99%
- Form validation accuracy > 99%
