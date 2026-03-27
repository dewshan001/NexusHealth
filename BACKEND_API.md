# NexusHealth Backend API Documentation

## Overview
NexusHealth is a comprehensive healthcare management system built with **Spring Boot** and **SQLite**. It provides REST APIs for managing appointments, doctors, patients, prescriptions, and medicines.

## Technology Stack
- **Framework**: Spring Boot 4.0.5
- **Database**: SQLite with JPA/Hibernate
- **Authentication**: JWT (JSON Web Token)
- **Build Tool**: Maven
- **Java Version**: 21

## Project Structure

```
src/main/java/com/nexushelth/
├── config/              # Configuration classes (CORS, Database config)
├── controllers/         # REST API endpoints
├── dto/                 # Data Transfer Objects
├── entities/            # JPA Entity classes
├── enums/               # Enumeration classes
├── exceptions/          # Exception handling
├── repositories/        # Spring Data JPA repositories
├── services/            # Business logic layer
└── utils/               # Utility classes (JWT, Password Encoder)
```

## Database Setup
The application uses SQLite with automatic schema generation. The database file `nexushelth.db` will be created automatically in the project root on first run.

### Entity Relationships
```
User (1) ──────── (1) Doctor
User (1) ──────── (1) Patient
Doctor (1) ──── (N) Appointment ──── (N) Patient
Doctor (1) ──── (N) Prescription ──── (N) Patient
Medicine (independent entity)
```

## Running the Application

### Prerequisites
- Java 21+
- Maven 3.6+

### Build & Run
```bash
# Navigate to project directory
cd NexusHelth

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

## API Endpoints

### 1. Authentication APIs (`/api/auth`)

#### **Login**
```
POST /api/auth/login
Content-Type: application/json

{
    "email": "doctor@clinic.com",
    "password": "password123"
}

Response:
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
        "id": 1,
        "email": "doctor@clinic.com",
        "firstName": "John",
        "lastName": "Doe",
        "phone": "1234567890",
        "role": "DOCTOR",
        "isActive": true
    },
    "message": "Login successful"
}
```

#### **Sign Up**
```
POST /api/auth/signup
Content-Type: application/json

{
    "email": "patient@example.com",
    "password": "password123",
    "firstName": "Jane",
    "lastName": "Smith",
    "phone": "0987654321",
    "role": "PATIENT"
}

Response: (Same as Login response)
```

---

### 2. Doctor APIs (`/api/doctors`)

#### **Get All Doctors**
```
GET /api/doctors
Response: Array of DoctorDTO objects
```

#### **Get Doctor by ID**
```
GET /api/doctors/{id}
Response: DoctorDTO object
```

#### **Get Doctor by User ID**
```
GET /api/doctors/user/{userId}
Response: DoctorDTO object
```

#### **Get Doctors by Specialization**
```
GET /api/doctors/specialization/{specialization}
Response: Array of DoctorDTO objects
```

#### **Get Available Doctors**
```
GET /api/doctors/available/list
Response: Array of DoctorDTO objects
```

#### **Create Doctor Profile**
```
POST /api/doctors/{userId}
Content-Type: application/json

{
    "specialization": "Cardiology",
    "licenseNumber": "LIC123456",
    "bio": "Dr. with 10 years experience",
    "consultationFee": 50.0,
    "availableHours": "9 AM - 5 PM",
    "isAvailable": true
}

Response: Created DoctorDTO object
```

#### **Update Doctor**
```
PUT /api/doctors/{id}
Content-Type: application/json

{
    "specialization": "Neurology",
    "consultationFee": 60.0,
    "isAvailable": false
}

Response: Updated DoctorDTO object
```

#### **Delete Doctor**
```
DELETE /api/doctors/{id}
Response: 204 No Content
```

---

### 3. Patient APIs (`/api/patients`)

#### **Get All Patients**
```
GET /api/patients
Response: Array of PatientDTO objects
```

#### **Get Patient by ID**
```
GET /api/patients/{id}
Response: PatientDTO object
```

#### **Get Patient by User ID**
```
GET /api/patients/user/{userId}
Response: PatientDTO object
```

#### **Create Patient Profile**
```
POST /api/patients/{userId}
Content-Type: application/json

{
    "dateOfBirth": "1990-05-15",
    "bloodType": "O+",
    "medicalHistory": "Diabetes, Hypertension",
    "allergies": "Penicillin",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001"
}

Response: Created PatientDTO object
```

#### **Update Patient**
```
PUT /api/patients/{id}
Content-Type: application/json

{
    "bloodType": "A+",
    "medicalHistory": "Updated history"
}

Response: Updated PatientDTO object
```

#### **Delete Patient**
```
DELETE /api/patients/{id}
Response: 204 No Content
```

---

### 4. Appointment APIs (`/api/appointments`)

#### **Get All Appointments**
```
GET /api/appointments
Response: Array of AppointmentDTO objects
```

#### **Get Appointment by ID**
```
GET /api/appointments/{id}
Response: AppointmentDTO object
```

#### **Get Patient's Appointments**
```
GET /api/appointments/patient/{patientId}
Response: Array of AppointmentDTO objects
```

#### **Get Doctor's Appointments**
```
GET /api/appointments/doctor/{doctorId}
Response: Array of AppointmentDTO objects
```

#### **Get Appointments by Status**
```
GET /api/appointments/status/{status}
Valid statuses: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED

Response: Array of AppointmentDTO objects
```

#### **Book Appointment**
```
POST /api/appointments
Content-Type: application/json

{
    "doctorId": 1,
    "patientId": 2,
    "appointmentDateTime": "2026-04-15T10:30:00",
    "reason": "Regular checkup",
    "notes": "Patient has been experiencing fatigue"
}

Response: Created AppointmentDTO object
```

#### **Update Appointment**
```
PUT /api/appointments/{id}
Content-Type: application/json

{
    "appointmentDateTime": "2026-04-16T14:00:00",
    "status": "CONFIRMED"
}

Response: Updated AppointmentDTO object
```

#### **Cancel Appointment**
```
PUT /api/appointments/{id}/cancel
Response: 200 OK
```

#### **Delete Appointment**
```
DELETE /api/appointments/{id}
Response: 204 No Content
```

---

### 5. Prescription APIs (`/api/prescriptions`)

#### **Get All Prescriptions**
```
GET /api/prescriptions
Response: Array of PrescriptionDTO objects
```

#### **Get Prescription by ID**
```
GET /api/prescriptions/{id}
Response: PrescriptionDTO object
```

#### **Get Patient's Prescriptions**
```
GET /api/prescriptions/patient/{patientId}
Response: Array of PrescriptionDTO objects
```

#### **Get Doctor's Prescriptions**
```
GET /api/prescriptions/doctor/{doctorId}
Response: Array of PrescriptionDTO objects
```

#### **Get Patient's Active Prescriptions**
```
GET /api/prescriptions/patient/{patientId}/active
Response: Array of active PrescriptionDTO objects
```

#### **Create Prescription**
```
POST /api/prescriptions
Content-Type: application/json

{
    "doctorId": 1,
    "patientId": 2,
    "medicationName": "Aspirin",
    "dosage": "500mg",
    "frequency": "Twice daily",
    "instructions": "Take with food",
    "issuedDate": "2026-03-27",
    "expiryDate": "2026-06-27"
}

Response: Created PrescriptionDTO object
```

#### **Update Prescription**
```
PUT /api/prescriptions/{id}
Content-Type: application/json

{
    "medicationName": "Ibuprofen",
    "dosage": "400mg",
    "status": "COMPLETED"
}

Response: Updated PrescriptionDTO object
```

#### **Delete Prescription**
```
DELETE /api/prescriptions/{id}
Response: 204 No Content
```

---

### 6. Medicine APIs (`/api/medicines`)

#### **Get All Medicines**
```
GET /api/medicines
Response: Array of MedicineDTO objects
```

#### **Get Medicine by ID**
```
GET /api/medicines/{id}
Response: MedicineDTO object
```

#### **Get Available Medicines**
```
GET /api/medicines/available/list
Response: Array of available MedicineDTO objects
```

#### **Search Medicine by Name**
```
GET /api/medicines/search/{name}
Response: MedicineDTO object
```

#### **Get Medicines by Manufacturer**
```
GET /api/medicines/manufacturer/{manufacturer}
Response: Array of MedicineDTO objects
```

#### **Add Medicine**
```
POST /api/medicines
Content-Type: application/json

{
    "name": "Aspirin",
    "description": "Pain relief and anti-inflammatory",
    "price": 5.99,
    "quantity": 100,
    "manufacturer": "Bayer",
    "batchNumber": "BATCH001",
    "isAvailable": true
}

Response: Created MedicineDTO object
```

#### **Update Medicine**
```
PUT /api/medicines/{id}
Content-Type: application/json

{
    "quantity": 85,
    "price": 4.99
}

Response: Updated MedicineDTO object
```

#### **Delete Medicine**
```
DELETE /api/medicines/{id}
Response: 204 No Content
```

---

## User Roles

The system supports the following user roles:
- **ADMIN**: Full system access, user management
- **DOCTOR**: Can manage appointments, create prescriptions, view patient records
- **PATIENT**: Can book appointments, view prescriptions, access medical history
- **PHARMACIST**: Can manage medicines and fulfill prescriptions
- **RECEPTIONIST**: Can manage appointments and patient registration

---

## Error Handling

The API returns standardized error responses:

```json
{
    "message": "Error description",
    "status": 400,
    "timestamp": 1711598400000
}
```

Common HTTP Status Codes:
- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `204 No Content`: Successful request with no content
- `400 Bad Request`: Invalid input data
- `401 Unauthorized`: Authentication failed
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

## Security

- All passwords are encrypted using BCryptPasswordEncoder
- JWT tokens are used for stateless authentication
- CORS is configured to allow requests from all origins (adjust in production)
- Sensitive configuration should be moved to environment variables

---

## Future Enhancements

1. JWT token validation middleware for protected endpoints
2. Role-based access control (RBAC)
3. Email notifications for appointments and prescriptions
4. File uploads for medical records
5. Appointment reminders
6. Prescription fulfillment tracking
7. Patient-doctor rating system
8. Advanced search and filtering
9. Analytics and reporting

---

## Database Schema

The SQLite database automatically creates the following tables:

- `users`: User accounts and authentication
- `doctors`: Doctor profiles and specializations
- `patients`: Patient profiles and medical history
- `appointments`: Appointment bookings and scheduling
- `prescriptions`: Prescription records
- `medicines`: Pharmacy inventory

---

## Configuration Properties

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:sqlite:nexushelth.db

# JWT
jwt.secret=your-secret-key-change-in-production
jwt.expiration=86400000  # 24 hours

# Server
server.port=8080
```

---

## Contact & Support

For issues or questions, please refer to the project documentation or create an issue in the repository.

---

**Version**: 1.0.0  
**Last Updated**: March 27, 2026

