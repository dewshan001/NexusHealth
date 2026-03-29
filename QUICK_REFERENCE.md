# Quick Reference: Receptionist Dashboard Settings

## API Endpoints Summary

### Profile Management
| Method | Endpoint | Purpose | Parameters |
|--------|----------|---------|------------|
| GET | `/api/receptionist/profile` | Fetch full profile | None |
| POST | `/api/receptionist/profile/update` | Update name & phone | `fullName`, `phone` (optional) |
| POST | `/api/receptionist/profile/password` | Change password | `currentPassword`, `newPassword`, `confirmPassword` |

### Profile Picture
| Method | Endpoint | Purpose | Parameters |
|--------|----------|---------|------------|
| POST | `/api/receptionist/profile/picture` | Upload picture | File (multipart/form-data) |
| DELETE | `/api/receptionist/profile/picture` | Delete picture | None |

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    // Relevant data here
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Specific error message",
  "data": null
}
```

## Profile Data Structure
```json
{
  "id": 5,
  "fullName": "Test Receptionist",
  "email": "receptionist@hospital.com",
  "phone": "+1(555)123-4567",
  "role": "receptionist",
  "status": "active",
  "profilePicture": "uploads/receptionist/5/a1b2c3d4-e5f6.jpg"
}
```

## Testing Commands

### Login as Receptionist
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=receptionist@hospital.com&password=Receptionist@123" \
  -c cookies.txt
```

### Get Profile
```bash
curl -X GET http://localhost:8080/api/receptionist/profile \
  -b cookies.txt
```

### Update Profile
```bash
curl -X POST http://localhost:8080/api/receptionist/profile/update \
  -b cookies.txt \
  -d "fullName=John%20Doe&phone=%2B1%28555%29123-4567"
```

### Upload Picture
```bash
curl -X POST http://localhost:8080/api/receptionist/profile/picture \
  -b cookies.txt \
  -F "file=@/path/to/image.jpg"
```

### Delete Picture
```bash
curl -X DELETE http://localhost:8080/api/receptionist/profile/picture \
  -b cookies.txt
```

### Change Password
```bash
curl -X POST http://localhost:8080/api/receptionist/profile/password \
  -b cookies.txt \
  -d "currentPassword=OldPass%40123&newPassword=NewPass%40456&confirmPassword=NewPass%40456"
```

## Database Queries

### Check Receptionist Profile
```sql
SELECT id, full_name, email, phone, profile_picture, role, status 
FROM users 
WHERE role = 'receptionist' 
LIMIT 1;
```

### Update Phone Directly (Admin)
```sql
UPDATE users SET phone = '+1(555)999-8888' 
WHERE id = 5 AND role = 'receptionist';
```

### Check Upload File
```sql
SELECT id, full_name, profile_picture 
FROM users 
WHERE id = 5;
```

### Clear Picture Reference
```sql
UPDATE users SET profile_picture = NULL 
WHERE id = 5;
```

## Browser Console Tests

### Fetch Profile
```javascript
fetch('/api/receptionist/profile')
  .then(r => r.json())
  .then(d => console.log(d))
```

### Upload Picture
```javascript
const formData = new FormData();
formData.append('file', document.getElementById('profileUpload').files[0]);

fetch('/api/receptionist/profile/picture', {
  method: 'POST',
  body: formData
})
.then(r => r.json())
.then(d => console.log(d))
```

### Change Password
```javascript
const params = new URLSearchParams({
  currentPassword: 'Receptionist@123',
  newPassword: 'NewPassword@456',
  confirmPassword: 'NewPassword@456'
});

fetch('/api/receptionist/profile/password', {
  method: 'POST',
  body: params
})
.then(r => r.json())
.then(d => console.log(d))
```

## Validation Rules

### Phone Number
- Format: `^\+?[\d\s\-()]+$`
- Examples: `+1-555-123-4567`, `555 123 4567`, `(555) 123-4567`

### Full Name
- Required field
- Max 100 characters
- Alphanumeric + spaces

### Password
- Minimum 8 characters
- Must contain: uppercase, lowercase, numbers
- Example: `NewPass@456`

### Profile Picture
- Format: JPG or PNG only
- Maximum size: 5MB
- Stored path: `uploads/receptionist/{userId}/{uuid}.{ext}`

## Error Handling

### Common Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| "Unauthorized - receptionist role required" | Not logged in as receptionist | Login with receptionist credentials |
| "Full name is required" | Empty name field | Enter a name |
| "Invalid phone number format" | Phone doesn't match regex | Use format: +1-555-123-4567 |
| "Password must be at least 8 characters" | Password too short | Enter 8+ characters |
| "New passwords do not match" | Passwords don't match | Ensure both fields are identical |
| "Current password is incorrect" | Wrong password verification | Enter correct current password |
| "File size exceeds 5MB limit" | Image too large | Use smaller image |
| "Invalid file type - only JPG and PNG allowed" | Wrong file format | Upload JPG or PNG only |
| "No file uploaded" | Empty file field | Select a file first |

## Directory Structure

```
uploads/
└── receptionist/
    ├── 5/
    │   ├── a1b2c3d4-e5f6-7890-abcd.jpg
    │   └── b2c3d4e5-f678-9012-bcde.png
    └── 6/
        └── c3d4e5f6-7890-1234-cdef.jpg
```

## Environment Setup

### Create Uploads Directory
```powershell
New-Item -Path ".\uploads\receptionist" -ItemType Directory -Force
```

### Check Permissions
```powershell
icacls ".\uploads" /grant "IIS APPPOOL\DefaultAppPool":(OI)(CI)M
```

## Performance Tips

1. **Image Optimization**: Compress images before upload
2. **Database**: Index on `users(id, role)` for faster lookups
3. **Caching**: Cache profile data for 5 minutes
4. **File Serving**: Use CDN for profile picture URLs

## Security Checklist

- [x] MIME type validation (server-side)
- [x] File size limit (5MB)
- [x] UUID filenames (prevent traversal)
- [x] BCrypt password hashing
- [x] Session authorization on all endpoints
- [x] HTTP-only cookies
- [x] SameSite cookie attribute
- [x] Input validation (regex for phone)
- [x] No sensitive data in logs
- [ ] Rate limiting on uploads (optional)
- [ ] Virus scan on uploads (optional)
- [ ] CDN for profile pictures (optional)

## Links

- **Implementation Doc**: [RECEPTIONIST_SETTINGS_IMPLEMENTATION.md](RECEPTIONIST_SETTINGS_IMPLEMENTATION.md)
- **Test Script**: [test_receptionist_settings.ps1](test_receptionist_settings.ps1)
- **GitHub**: (Link to repo if available)

---

**Last Updated**: March 29, 2026  
**Version**: 1.0.0
