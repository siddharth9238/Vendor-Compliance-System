# Vendor Compliance & Risk Management System

Full-stack vendor compliance and risk management platform.

## Tech Stack

### Backend
- Java 17
- Maven
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL/MySQL

### Frontend
- React 18
- React Router v6
- Axios
- Modern JavaScript (ES6+)

## Project Structure

```
vendor compliance system/
├── src/                    # Backend (Spring Boot)
├── frontend/              # Frontend (React)
│   ├── public/
│   ├── src/
│   ├── package.json
│   └── README.md
└── README.md             # This file
```

## Quick Start

### Backend Setup

1. **Prerequisites**
   - Java 17+
   - Maven 3.8+
   - PostgreSQL (or MySQL)

2. **Configure Database**
   
   Update `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/vendor_compliance
       username: your_user
       password: your_password
   ```

3. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   
   Backend runs on **http://localhost:8080**

### Frontend Setup

1. **Prerequisites**
   - Node.js 14+
   - npm 6+ (or yarn)

2. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

3. **Configure API URL**
   
   Update `frontend/.env`:
   ```env
   REACT_APP_API_URL=http://localhost:8080/api
   ```

4. **Start Development Server**
   ```bash
   npm start
   ```
   
   Frontend runs on **http://localhost:3000**

## Features

### Backend Features
- **Authentication & Authorization**
  - JWT-based stateless authentication
  - Role-based access control (RBAC)
  - 4 roles: ADMIN, VENDOR_MANAGER, AUDITOR, VENDOR

- **Vendor Management**
  - Vendor onboarding workflow
  - Status tracking: PENDING, APPROVED, REJECTED
  - Document upload and expiry tracking

- **Risk Scoring Engine**
  - Automatic risk calculation
  - Formula: `(missing_docs × 20) + (expired_docs × 30) + (audit_flags × 25)`
  - Risk levels: LOW (≤20), MEDIUM (≤50), HIGH (>50)

- **Compliance Monitoring**
  - Daily scheduled jobs for expired documents
  - High-risk vendor alerts
  - Automatic audit flags

- **Audit Trail**
  - Complete action logging
  - Login tracking
  - Vendor lifecycle events
  - Document management events

### Frontend Features
- **Secure Authentication**
  - JWT token management
  - Automatic token injection
  - Automatic logout on expiration

- **Role-Based UI**
  - Login page
  - Vendor dashboard (VENDOR role)
  - Manager dashboard (VENDOR_MANAGER role)
  - Protected routes with role validation

- **Dashboard Features**
  - Vendor list with status and risk scores
  - Status filtering (for managers)
  - Vendor approval actions
  - Risk level visualization

## Demo Credentials

```
Vendor User:
  Username: vendor1
  Password: Password1!

Manager User:
  Username: manager1
  Password: Password1!

Admin User:
  Username: admin1
  Password: Password1!
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh token
- `GET /api/auth/me` - Get current user profile

### Vendors
- `POST /api/vendors` - Submit vendor onboarding (VENDOR)
- `GET /api/vendors` - List vendors (VENDOR_MANAGER, AUDITOR)
- `GET /api/vendors/{id}` - Get vendor details
- `PATCH /api/vendors/{id}/approve` - Approve vendor (VENDOR_MANAGER, ADMIN)
- `PATCH /api/vendors/{id}/reject` - Reject vendor (VENDOR_MANAGER, ADMIN)
- `GET /api/vendors/{id}/risk-score` - Calculate risk score (VENDOR_MANAGER, AUDITOR, ADMIN)

### Documents
- `POST /api/vendors/{id}/documents` - Upload document
- `GET /api/vendors/{id}/documents` - List vendor documents

### Audit
- `GET /api/audits` - Get audit logs (filters: vendorId, action)

## Database Schema

**Key Tables:**
- `app_users` - User accounts
- `vendors` - Vendor records
- `vendor_documents` - Document storage
- `audit_logs` - Audit trail
- `audit_flags` - Compliance issues

## Security Features

✅ JWT-based authentication  
✅ Role-based access control  
✅ Password hashing (BCrypt)  
✅ CORS protection  
✅ SQL injection prevention (parameterized queries)  
✅ Automatic token refresh  
✅ Secure token storage (localStorage)  
✅ Automatic logout on expiration  

## Running Tests

### Backend
```bash
mvn test
```

### Frontend
```bash
cd frontend
npm test
```

## Production Build

### Backend
```bash
mvn clean package
# Creates JAR in target/
```

### Frontend
```bash
cd frontend
npm run build
# Creates optimized build in build/
```

## Docker (Optional)

### Backend
```dockerfile
FROM openjdk:17-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Frontend
```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY . .
RUN npm install && npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
```

## Troubleshooting

### Backend won't start
- Check database connection
- Verify Java version (17+)
- Check port 8080 is available

### Frontend connection errors
- Ensure backend is running on port 8080
- Check `.env` REACT_APP_API_URL
- Check browser console for CORS errors

### Login fails
- Verify credentials match those in database
- Check backend logs for authentication errors
- Ensure JWT secret is configured

## Documentation

- [Backend Implementation](src/main/java/com/vendorcompliance/)
- [Frontend Setup](frontend/README.md)

## Architecture Highlights

### Backend
- **Layered Architecture**: Controller → Service → Repository
- **Dependency Injection**: Spring Framework IoC
- **Transaction Management**: @Transactional annotations
- **Event-Driven**: Risk recalculation on document/flag changes
- **Scheduled Tasks**: Daily compliance checks

### Frontend
- **Component-Based**: Reusable React components
- **Custom Hooks**: useAuth for state management
- **Interceptors**: Axios request/response interceptors
- **Protected Routes**: Role-based route protection
- **Responsive Design**: Mobile-friendly UI

## Key Business Logic

1. **Vendor Onboarding**
   - VENDOR role submits onboarding request
   - Status set to PENDING
   - VENDOR_MANAGER reviews and approves/rejects

2. **Risk Calculation**
   - Triggered on: document upload, flag creation, flag resolution
   - Missing documents: 20 points each
   - Expired documents: 30 points each
   - Unresolved audit flags: 25 points each
   - Capped at 100

3. **Compliance Monitoring**
   - Daily 2 AM: Check for expired documents
   - Daily 3 AM: Check for high-risk vendors (>60)
   - Auto-create audit flags on issues
   - Trigger risk recalculation

4. **Audit Trail**
   - Every action logged with timestamp and actor
   - Supports filters by vendor and action
   - System actions performed by "SYSTEM" user

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

Proprietary - Not for public distribution

## Support

For issues and questions, check:
- Backend logs: `target/logs/`
- Frontend console: Browser DevTools
- Database: Verify schema and data

---

## Deployment

For comprehensive deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md)

### Quick Deployment

**Docker (Recommended for fast setup):**
```bash
docker-compose up -d
```

**Render (Cloud, free tier available):**
1. Push code to GitHub
2. Create web service on Render
3. Add environment variables
4. Deploy!

**AWS EC2 (VPS, free tier eligible):**
1. Launch EC2 instance
2. Install Java, Maven
3. Build JAR: `mvn clean package`
4. Create systemd service
5. Start service: `sudo systemctl start vendor-compliance`

### Environment Variables

Required for production:
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/vendor_compliance
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=your_secret_32_chars_minimum
SPRING_PROFILES_ACTIVE=prod
```

See `.env.example` for complete list.

### Database

**Managed Options:**
- Render PostgreSQL (Recommended)
- AWS RDS
- Supabase
- DigitalOcean Managed Databases

All are reliable, auto-backed up, and handle scaling.

### Pre-Deployment Checklist

Before deploying to production, see [DEPLOYMENT-CHECKLIST.md](DEPLOYMENT-CHECKLIST.md)

Key items:
- [ ] All tests passing
- [ ] Environment variables configured
- [ ] Database credentials secured
- [ ] JWT secret is 32+ characters
- [ ] HTTPS/TLS enabled
- [ ] Firewall rules configured
- [ ] Backups enabled
- [ ] Monitoring configured

### Deployment Scripts

Auto-deployment helper:
```bash
./deploy.sh local      # Run locally with Maven
./deploy.sh docker     # Run in Docker containers
./deploy.sh render     # Deploy to Render
./deploy.sh aws        # Deploy to AWS EC2
```

### Production Readiness

✅ Containerized with Docker  
✅ CI/CD pipeline with GitHub Actions  
✅ Environment-based configuration  
✅ Comprehensive logging  
✅ Health check endpoints  
✅ Database connection pooling  
✅ Transaction management  
✅ Scheduled job monitoring  
✅ Error handling & validation  
✅ Security best practices  

### Cost Estimation

**Free Tier (First Year):**
- Render: $0 (limited)
- AWS EC2 + RDS: $0 (1 year free tier)

**Production (After Free Tier):**
- Render: ~$32/month (web + database)
- AWS: ~$30/month (EC2 + RDS)
- DigitalOcean: ~$15/month (app + database)

---
