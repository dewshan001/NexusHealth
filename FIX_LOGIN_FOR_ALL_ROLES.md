# Fix: Login for All User Roles

## Problem
✅ Admin login works  
❌ Other roles (doctor, patient, pharmacist, receptionist) don't work

## Root Cause
The database likely doesn't have test users for non-admin roles, so login fails for those roles.

## Solution
Use the provided SQL script to create test users for all roles.

---

## Step 1: Create Test Users

### Option A: Using SQLite Browser GUI
1. Open your SQLite database file (usually in the target directory or project root)
2. Open the file `add-test-users.sql` in a text editor
3. Copy the entire SQL content
4. Paste it into your SQLite browser's SQL editor
5. Execute the query

### Option B: Using Command Line
```bash
cd "c:\Users\Dewshan Gunawardhane\Desktop\New project\Fianel\NexusHealth"
sqlite3 clinic.db < add-test-users.sql
```

### Option C: If you don't have a password hashing tool yet, run this simpler version

Instead of trying to hash the password yourself, the easiest way is:
1. Start the application
2. Use the signup page to create new users for each role
3. Create accounts as:
   - doctor@clinic.com (choose "doctor" role if dropdown exists, or create via admin panel)
   - patient@clinic.com (choose "patient" role)
   - pharmacist@clinic.com (choose "pharmacist" role)
   - receptionist@clinic.com (choose "receptionist" role)

---

## Test Credentials After Running Script

The `add-test-users.sql` script creates these test users:

| Email | Password | Role |
|-------|----------|------|
| admin@clinic.com | Password123! | admin |
| doctor@clinic.com | Password123! | doctor |
| patient@clinic.com | Password123! | patient |
| pharmacist@clinic.com | Password123! | pharmacist |
| receptionist@clinic.com | Password123! | receptionist |

---

## Verification Steps

1. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

2. **Test Login for Each Role**
   - Go to http://localhost:8080/login
   - Try each email with password: `Password123!`
   - Verify that each role shows the correct dashboard

3. **Expected Results**
   - 🟦 Admin Dashboard: Shows user management, staff, patients, analytics
   - 🟩 Doctor Dashboard: Shows appointments, prescriptions, patient consultations
   - 🟪 Patient Dashboard: Shows appointments, visit history, prescriptions, billing
   - 🟧 Pharmacist Dashboard: Shows inventory, prescriptions (if implemented)
   - 🟨 Receptionist Dashboard: Shows appointments, patient registration (if implemented)

---

## If Still Having Issues

### Check 1: Verify Database Exists and Has Tables
```bash
sqlite3 clinic.db ".tables"
```

You should see: `users`, `doctors`, `patients`, `appointments`, etc.

### Check 2: Check if Users Were Created
```bash
sqlite3 clinic.db "SELECT id, full_name, email, role FROM users;"
```

You should see all 5 test users listed.

### Check 3: Check Application Logs
When logging in, check the console output for error messages starting with:
- ❌ (error messages)
- ✅ (success messages)

### Check 4: Verify Dependent Records Exist
If doctor login fails, verify a doctor profile exists:
```bash
sqlite3 clinic.db "SELECT * FROM doctors;"
```

If patient login fails, verify a patient profile exists:
```bash
sqlite3 clinic.db "SELECT * FROM patients;"
```

---

## Manual User Creation (Alternative)

If the SQL script doesn't work, you can create users manually through the admin panel:

1. Login as admin@clinic.com
2. Go to the admin dashboard
3. Use "Create Staff" or user management section
4. Add users for each role

---

## Session Management (Important)

After implementing the per-tab session system:
- Each browser tab gets a unique `sessionId`
- Sessions are stored in `sessionStorage` (per-tab)
- Sessions timeout after 30 minutes of inactivity
- Logging out one tab doesn't affect other tabs

**This is working correctly - no special action needed.**

---

## If You Continue to Have Issues

Check:
1. ✅ Database file exists and is accessible
2. ✅ Test users were successfully created
3. ✅ Passwords are correct (check case sensitivity)
4. ✅ User role in database matches expected role
5. ✅ No exceptions in console logs

If still stuck, provide:
- Screenshot of error message
- Console output when attempting to login
- Output of `sqlite3 clinic.db "SELECT id, email, role, status FROM users;"`
