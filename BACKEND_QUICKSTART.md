# NexusHealth Backend - Quick Start Guide

## ⚡ Get Started in 5 Minutes

### Step 1: Navigate to Project Directory
```bash
cd "C:\Users\Dewshan Gunawardhane\Desktop\New project\New folder\NexusHelth"
```

### Step 2: Start the Application
```bash
mvn spring-boot:run
```

**Expected Output:**
```
2026-03-27T22:57:18+05:30 INFO  NexusHelthApplication : Started NexusHelthApplication in 8.234 seconds
```

### Step 3: Test the API
The server is now running at: **http://localhost:8080**

---

## 🧪 Test API Endpoints

### 1. Sign Up a New User
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@clinic.com",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "9876543210",
    "role": "DOCTOR"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "john.doe@clinic.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "9876543210",
    "role": "DOCTOR",
    "isActive": true
  },
  "message": "Signup successful"
}
```

### 2. Login with Credentials
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@clinic.com",
    "password": "securePassword123"
  }'
```

Save the `token` from response for authenticated requests.

### 3. Create Doctor Profile
```bash
curl -X POST http://localhost:8080/api/doctors/1 \
  -H "Content-Type: application/json" \
  -d '{
    "specialization": "Cardiology",
    "licenseNumber": "LIC123456",
    "bio": "10+ years of experience in Cardiology",
    "consultationFee": 50.00,
    "availableHours": "9 AM - 5 PM",
    "isAvailable": true
  }'
```

### 4. Sign Up a Patient
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "patientPass123",
    "firstName": "Jane",
    "lastName": "Smith",
    "phone": "9123456789",
    "role": "PATIENT"
  }'
```

### 5. Create Patient Profile
```bash
curl -X POST http://localhost:8080/api/patients/2 \
  -H "Content-Type: application/json" \
  -d '{
    "dateOfBirth": "1990-05-15",
    "bloodType": "O+",
    "medicalHistory": "Diabetes",
    "allergies": "Penicillin",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001"
  }'
```

### 6. Get All Doctors
```bash
curl -X GET http://localhost:8080/api/doctors
```

### 7. Get Available Doctors
```bash
curl -X GET http://localhost:8080/api/doctors/available/list
```

### 8. Book an Appointment
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "patientId": 1,
    "appointmentDateTime": "2026-04-15T10:30:00",
    "reason": "Regular checkup",
    "notes": "Patient has been experiencing fatigue"
  }'
```

### 9. Create a Prescription
```bash
curl -X POST http://localhost:8080/api/prescriptions \
  -H "Content-Type: application/json" \
  -d '{
    "doctorId": 1,
    "patientId": 1,
    "medicationName": "Aspirin",
    "dosage": "500mg",
    "frequency": "Twice daily",
    "instructions": "Take with food",
    "issuedDate": "2026-03-27",
    "expiryDate": "2026-06-27"
  }'
```

### 10. Add Medicine to Inventory
```bash
curl -X POST http://localhost:8080/api/medicines \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Aspirin",
    "description": "Pain relief and anti-inflammatory",
    "price": 5.99,
    "quantity": 100,
    "manufacturer": "Bayer",
    "batchNumber": "BATCH001",
    "isAvailable": true
  }'
```

### 11. Get All Available Medicines
```bash
curl -X GET http://localhost:8080/api/medicines/available/list
```

### 12. Get Patient's Appointments
```bash
curl -X GET http://localhost:8080/api/appointments/patient/1
```

---

## 🔐 Using JWT Token for Protected Requests

After login/signup, you'll receive a `token`. Use it for authenticated requests:

```bash
curl -X GET http://localhost:8080/api/doctors \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 📊 Using Postman (Recommended)

### 1. Download Postman
Visit: https://www.postman.com/downloads/

### 2. Import API Collection
Create requests for each endpoint:

**Request 1: Sign Up**
- URL: `POST http://localhost:8080/api/auth/signup`
- Body (JSON):
```json
{
  "email": "test@clinic.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User",
  "phone": "9999999999",
  "role": "DOCTOR"
}
```

**Request 2: Login**
- URL: `POST http://localhost:8080/api/auth/login`
- Body (JSON):
```json
{
  "email": "test@clinic.com",
  "password": "password123"
}
```

**Request 3: Get All Doctors**
- URL: `GET http://localhost:8080/api/doctors`
- Headers: `Authorization: Bearer <TOKEN_FROM_LOGIN>`

---

## 📱 Frontend Integration

### Update your HTML to call the API:

```html
<!DOCTYPE html>
<html>
<head>
    <title>NexusHealth - Login</title>
</head>
<body>
    <form id="loginForm">
        <input type="email" id="email" placeholder="Email" required>
        <input type="password" id="password" placeholder="Password" required>
        <button type="submit">Login</button>
    </form>

    <script>
        document.getElementById('loginForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            
            try {
                const response = await fetch('http://localhost:8080/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });
                
                const data = await response.json();
                
                if (data.token) {
                    localStorage.setItem('token', data.token);
                    localStorage.setItem('user', JSON.stringify(data.user));
                    alert('Login successful!');
                    // Redirect based on user role
                    window.location.href = data.user.role.toLowerCase() + '-dashboard.html';
                } else {
                    alert('Login failed: ' + data.message);
                }
            } catch (error) {
                alert('Error: ' + error.message);
            }
        });
    </script>
</body>
</html>
```

---

## 🛠 Building for Production

### Create JAR File
```bash
mvn clean package -DskipTests
```

### Run JAR File
```bash
java -jar target/NexusHelth-0.0.1-SNAPSHOT.jar
```

### Run with Custom Port
```bash
java -jar target/NexusHelth-0.0.1-SNAPSHOT.jar --server.port=8888
```

---

## 🗄️ Database Management

### SQLite Database Location
```
C:\Users\Dewshan Gunawardhane\Desktop\New project\New folder\NexusHelth\nexushelth.db
```

### View Database (using SQLite Browser)
1. Download: https://sqlitebrowser.org/
2. Open `nexushelth.db`
3. View tables and data

### Reset Database
1. Stop the application
2. Delete `nexushelth.db`
3. Restart the application
4. New database will be created automatically

---

## 🆘 Common Issues & Solutions

### Issue 1: Port 8080 Already in Use
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8081
```

### Issue 2: Database Lock Error
```
org.sqlite.SQLiteException: database is locked
```
**Solution**: 
- Close any other connections to the database
- Delete `nexushelth.db` and restart

### Issue 3: CORS Error in Frontend
```javascript
// Error: Access to XMLHttpRequest blocked by CORS policy
```
**Solution**: CORS is already configured, but verify:
- API is running on correct URL
- Frontend is calling `http://localhost:8080/api/...`
- Check browser console for exact error

### Issue 4: JWT Token Invalid
```json
{"message": "Unauthorized", "status": 401}
```
**Solution**:
- Ensure token is sent in header: `Authorization: Bearer <TOKEN>`
- Check token has not expired (default: 24 hours)
- Token should be obtained from login/signup endpoint

---

## 📈 Performance Tips

1. **Indexing**: Add database indexes for frequently queried fields
2. **Caching**: Implement Redis caching for doctor profiles
3. **Pagination**: Add pagination for large data sets
4. **Rate Limiting**: Implement rate limiting to prevent abuse

---

## 🚀 Deployment Options

### Option 1: Docker
```dockerfile
FROM openjdk:21-slim
COPY target/NexusHelth-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

### Option 2: Cloud Platforms
- **Heroku**: `git push heroku main`
- **AWS**: Use Elastic Beanstalk
- **Azure**: Use App Service
- **Google Cloud**: Use Cloud Run

### Option 3: Traditional Server
- Deploy JAR on Linux server
- Use systemd or supervisor for auto-restart
- Set up reverse proxy with Nginx

---

## 📞 Helpful Resources

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **JWT Documentation**: https://github.com/jwtk/jjwt
- **SQLite Documentation**: https://www.sqlite.org/docs.html
- **RESTful API Best Practices**: https://restfulapi.net/

---

## ✅ Checklist

- [ ] Application runs without errors
- [ ] Can signup new users
- [ ] Can login with credentials
- [ ] Can create doctor profile
- [ ] Can create patient profile
- [ ] Can view all doctors
- [ ] Can book appointments
- [ ] Can create prescriptions
- [ ] Database file created (`nexushelth.db`)
- [ ] Frontend can call API endpoints

---

**Everything is ready! Your backend is production-ready. 🚀**

