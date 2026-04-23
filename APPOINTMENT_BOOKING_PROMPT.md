# Appointment Booking Interface - Development Prompt

## Project Overview
Build an interactive appointment booking system for NexusHealth - a Clinical Management System. The system should allow patients to search for doctors, view availability, and book in-person appointments through an intuitive calendar-based interface.

---

## Feature Requirements

### 1. **Doctor Search & Filtering**
- Search doctors by:
  - Name
  - Specialization (Cardiology, Dermatology, General Practice, etc.)
  - Clinic/Location
  - Rating/Reviews
  - Availability status
- Display doctor cards with:
  - Profile image
  - Name, qualification, specialization
  - Experience years
  - Rating and reviews count
  - Consultation fee
  - Availability status (Available, Busy, On Leave)
  - Quick action buttons (View Profile, Book Appointment)

### 2. **Interactive Calendar Interface**
- **Month View Calendar:**
  - Display current month with date grid
  - Highlight available dates (dates with slots) in green
  - Gray out unavailable dates
  - Show today's date distinctly
  - Navigation arrows for previous/next month
  
- **Date Selection:**
  - Click on available date to view available time slots
  - Show count of available slots per date
  - Disable past dates
  - Mobile-friendly calendar

### 3. **Time Slot Selection**
- Display available time slots for selected date
- Show slot details:
  - Time (HH:MM format)
  - Doctor name
  - Slot status (Available/Booked)
- Color code:
  - Green for available slots
  - Red/Gray for booked slots
  - Yellow for limited availability (only 1-2 slots)
- Allow selection of preferred slot with visual feedback

### 4. **Booking Form**
Collect patient information:
- Patient full name (auto-filled if logged in)
- Phone number
- Email address
- Symptoms/Chief complaint (optional text area)
- Medical history (dropdown/checkbox)
- Preferred communication language
- Patient ID (if returning patient)
- Emergency contact (optional)
- Consent checkbox for privacy policy

### 5. **Booking Confirmation**
- Display booking summary:
  - Doctor name and specialization
  - Appointment date and time
  - Clinic location
  - Consultation fee
  - Patient name and phone
- Options:
  - Confirm booking
  - Back to edit details
  - Cancel booking
- Success message with:
  - Booking confirmation number
  - Download receipt/appointment slip
  - Option to add to calendar (Google/Outlook)

### 6. **User Interface/UX**
- **Responsive Design:**
  - Desktop (1200px+): Full calendar with sidebar
  - Tablet (768px-1199px): Adjusted layout
  - Mobile (below 768px): Stack layout, carousel for slots
  
- **Visual Feedback:**
  - Loading spinners for data fetching
  - Error messages for invalid selections
  - Toast notifications for actions
  - Hover effects on interactive elements
  
- **Accessibility:**
  - ARIA labels for screen readers
  - Keyboard navigation support
  - High contrast colors for readability
  - Alt text for images

### 7. **Workflow Steps**
```
1. Doctor Selection
   ↓
2. View Doctor Details & Availability
   ↓
3. Select Date from Calendar
   ↓
4. Select Time Slot
   ↓
5. Fill Booking Form
   ↓
6. Review Booking Summary
   ↓
7. Confirm Appointment
   ↓
8. Download Confirmation & View Appointment Details
```

---

## Technical Implementation

### Frontend Stack
- **HTML5** - Structure
- **CSS3** - Styling with CSS Grid/Flexbox for responsive layout
- **JavaScript (Vanilla/ES6+)** - Interactivity
  - Calendar library: Flatpickr or custom implementation
  - Date utility: Day.js or date-fns for date manipulation
  - HTTP requests: Fetch API or Axios

### Data Structure (Example)

**Doctor Object:**
```javascript
{
  id: 1,
  name: "Dr. John Smith",
  specialization: "Cardiology",
  clinic: "Heart Care Clinic",
  location: "123 Medical Street",
  rating: 4.8,
  reviews: 245,
  fee: 500,
  image: "url",
  availability: {
    "2026-04-24": ["09:00", "09:30", "10:00"],
    "2026-04-25": ["14:00", "14:30", "15:00"]
  }
}
```

**Appointment Object:**
```javascript
{
  id: "APT001",
  patientId: 123,
  doctorId: 1,
  date: "2026-04-24",
  time: "09:00",
  status: "confirmed",
  notes: "Patient symptoms",
  clinicLocation: "Heart Care Clinic",
  createdAt: "2026-04-23"
}
```

### API Endpoints Needed
```
GET /api/doctors - List all doctors with filtering
GET /api/doctors/:id - Get doctor details
GET /api/doctors/:id/availability - Get available slots for date range
POST /api/appointments - Create new appointment
GET /api/appointments/:id - Get appointment details
PUT /api/appointments/:id - Update appointment
DELETE /api/appointments/:id - Cancel appointment
GET /api/specializations - List all specializations
```

---

## Files to Create

```
frontend/
├── appointment-booking.html      # Main booking page
├── css/
│   └── appointment-booking.css   # Dedicated styles
├── js/
│   ├── appointment-booking.js    # Main functionality
│   ├── calendar.js               # Calendar logic
│   ├── doctor-search.js          # Search & filter logic
│   ├── slot-selector.js          # Time slot selection
│   └── booking-form.js           # Form handling
└── assets/
    └── default-doctor.png         # Placeholder images
```

---

## Key Features to Implement in Order

### Phase 1 (MVP)
- [ ] Static doctor list display
- [ ] Basic calendar view
- [ ] Manual slot selection
- [ ] Booking form with validation
- [ ] Confirmation display

### Phase 2
- [ ] Doctor search & filtering
- [ ] Dynamic availability loading
- [ ] Appointment history
- [ ] Print appointment slip

### Phase 3
- [ ] Payment integration
- [ ] Advanced filtering (ratings, experience)
- [ ] Recurring appointments
- [ ] Appointment management (reschedule/cancel)

---

## Example Booking Workflow Code Structure

```javascript
// Step 1: Initialize
appointmentBooking.init();

// Step 2: Search Doctors
appointmentBooking.searchDoctors(filters)
  .then(doctors => displayDoctorList(doctors));

// Step 3: Select Doctor
appointmentBooking.selectDoctor(doctorId)
  .then(doctor => showAvailability(doctor));

// Step 4: Select Date
appointmentBooking.selectDate(date)
  .then(slots => displayTimeSlots(slots));

// Step 5: Select Time
appointmentBooking.selectTimeSlot(time);

// Step 6: Fill Form
appointmentBooking.fillPatientInfo(formData);

// Step 7: Confirm Booking
appointmentBooking.confirmBooking()
  .then(confirmation => showConfirmation(confirmation));
```

---

## Validation Rules

**Form Validation:**
- Patient name: Required, min 3 characters
- Phone: Required, valid format (10 digits)
- Email: Required, valid email format
- Date: Must be future date, available
- Time: Must be within doctor's working hours
- Consent: Must be checked

**Business Rules:**
- Cannot book past appointments
- Cannot book more than 2 months in advance
- Cannot have overlapping appointments for same patient
- Minimum 24 hours notice for cancellation
- Doctor cannot have conflicting appointments

---

## Design Guidelines

- **Color Scheme:**
  - Primary: #0066FF (Medical Blue)
  - Success: #31C48D (Green for available)
  - Error: #E5354B (Red for unavailable)
  - Background: #F9FAFB

- **Typography:**
  - Font: DM Sans
  - Headings: Bold/Semibold
  - Body: Regular

- **Spacing:**
  - Padding: 1rem, 1.5rem, 2rem
  - Margin: 1rem, 1.5rem, 2rem
  - Gap: 0.5rem, 1rem, 1.5rem

---

## Error Handling

Handle these scenarios:
- Doctor profile not found
- No availability for selected date
- Appointment already booked by another user (race condition)
- Invalid form submission
- Network errors
- Server errors (500, 502, etc.)

---

## Success Metrics

- Users should book appointment in < 5 minutes
- Mobile booking completion rate > 80%
- Form validation accuracy > 99%
- Zero double-bookings
- Appointment confirmation display on first try
