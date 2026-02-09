# System Architecture

## Overview

Vendor Compliance Risk Management System is a full-stack enterprise application for managing vendor onboarding, compliance documentation, and risk assessment.

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React 18)                      │
│  └─ JWT Auth │ Role-Based Rendering │ Axios Interceptors   │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTPS/API
┌──────────────────────────────────────────────────────────────┐
│              Backend (Spring Boot 3.x)                       │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Controllers (REST API Endpoints)                       │ │
│  │  ├─ AuthController     (JWT, Roles)                    │ │
│  │  ├─ VendorController   (CRUD, Approval)               │ │
│  │  ├─ DocumentController (Upload, Expiry)               │ │
│  │  └─ AuditController    (Log Retrieval)                │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Services (Business Logic)                              │ │
│  │  ├─ AuthService        (JWT, Registration)            │ │
│  │  ├─ VendorService      (Onboarding, Approval)         │ │
│  │  ├─ RiskService        (Scoring, Calculation)         │ │
│  │  ├─ AuditService       (Logging, History)             │ │
│  │  ├─ AuditFlagService   (Compliance Flags)             │ │
│  │  ├─ VendorDocumentService (File Management)           │ │
│  │  └─ ScheduledJobService   (Daily Tasks)               │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Security                                               │ │
│  │  ├─ JwtUtil            (Token Generation)              │ │
│  │  ├─ JwtAuthenticationFilter  (Request Validation)      │ │
│  │  ├─ CustomUserDetailsService (User Loading)            │ │
│  │  └─ SecurityConfig     (RBAC, CORS)                    │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Repositories (Data Access)                             │ │
│  │  ├─ UserRepository                                      │ │
│  │  ├─ VendorRepository                                    │ │
│  │  ├─ VendorDocumentRepository                            │ │
│  │  ├─ AuditLogRepository                                  │ │
│  │  └─ AuditFlagRepository                                 │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────────┘
                       │ JDBC
┌──────────────────────────────────────────────────────────────┐
│         Database (PostgreSQL / Managed)                      │
│  ├─ app_users           (User accounts, roles)              │
│  ├─ vendors             (Vendor records, risk scores)       │
│  ├─ vendor_documents    (Compliance docs, expiry dates)     │
│  ├─ audit_logs          (Action history)                    │
│  └─ audit_flags         (Compliance issues)                 │
└──────────────────────────────────────────────────────────────┘
```

## Component Details

### Frontend (src/pages/, src/components/, src/hooks/)

**Pages:**
- `LoginPage.jsx` - JWT authentication, role-based login
- `VendorDashboard.jsx` - View vendors, risk scores (VENDOR role)
- `ManagerDashboard.jsx` - Manage vendors, approve/reject (VENDOR_MANAGER role)

**Hooks:**
- `useAuth()` - Authentication state, token management, role checking

**Components:**
- `ProtectedRoute` - Route authorization, role validation

**API Integration:**
- `api/axios.js` - Axios instance with JWT interceptor, auto-logout on 401

### Backend - Controllers

**AuthController**
```
POST   /api/auth/register           Create new user account
POST   /api/auth/login              Authenticate, get JWT token
POST   /api/auth/refresh            Refresh expired token
GET    /api/auth/me                 Get current user profile
```

**VendorController**
```
POST   /api/vendors                 Submit vendor onboarding (VENDOR role)
GET    /api/vendors                 List vendors (VENDOR_MANAGER, AUDITOR)
GET    /api/vendors/{id}            Get vendor details
PATCH  /api/vendors/{id}/approve    Approve vendor (VENDOR_MANAGER)
PATCH  /api/vendors/{id}/reject     Reject vendor (VENDOR_MANAGER)
GET    /api/vendors/{id}/risk-score Calculate vendor risk
```

**DocumentController**
```
POST   /api/vendors/{id}/documents  Upload compliance document
GET    /api/vendors/{id}/documents  List vendor documents
```

**AuditController**
```
GET    /api/audits                  Get audit logs (filters: vendorId, action)
```

### Backend - Services (Business Logic)

**AuthService**
- User registration with password validation
- JWT-based authentication
- Token refresh and validation
- Login audit logging

**VendorService**
- Vendor onboarding workflow (PENDING → APPROVED/REJECTED)
- Status transitions with validation
- Audit trail for approval events
- Bulk retrieval with filtering

**RiskService**
- Risk score calculation:
  - Missing documents: 20 points each
  - Expired documents: 30 points each
  - Unresolved audit flags: 25 points each
  - Capped at 100
- Risk level categorization (LOW, MEDIUM, HIGH)
- Persistent risk score in vendor table
- Triggered recalculation on events

**AuditService**
- Log all important events
- Support filtering by vendor and action
- Event types: LOGIN, VENDOR_ONBOARD_SUBMITTED, VENDOR_APPROVED, VENDOR_REJECTED, DOCUMENT_UPLOADED, RISK_SCORE_CALCULATED
- Actor username capture for accountability

**AuditFlagService**
- Create compliance issue flags
- Track resolution status
- Automatic risk recalculation on flag changes
- Prevention of duplicate flags

**VendorDocumentService**
- File upload with validation
- Expiry date tracking
- Auto-trigger risk recalculation on upload
- Latest document per type queries

**ScheduledJobService**
- **Daily 2 AM**: Check for expired documents → Create audit flags
- **Daily 3 AM**: Check for high-risk vendors (>60) → Create alerts
- Deduplication logic prevents duplicate flags
- System user ("SYSTEM") for automated actions

### Backend - Security

**JwtUtil**
- Token generation with claims
- Token validation and expiration check
- Username extraction from token
- Configurable secret and expiration time

**JwtAuthenticationFilter**
- Per-request token extraction and validation
- Automatic 401 on invalid/expired token
- Bearer token format validation

**CustomUserDetailsService**
- Spring UserDetails provider
- User lookup from database
- Role loading with user

**SecurityConfig**
- RBAC setup with authorities
- CORS configuration
- HTTPS enforcement (in prod)
- Anonymous access for auth endpoints

### Entities (Database Schema)

**AppUser**
- username (unique)
- email (unique)
- passwordHash (BCrypt)
- fullName
- roles (enum: ADMIN, VENDOR_MANAGER, AUDITOR, VENDOR)
- enabled flag

**Vendor**
- legalName, tradingName
- registrationNumber (unique)
- email
- phone, address
- status (PENDING, APPROVED, REJECTED)
- riskScore (0-100)
- lastRiskCalculatedAt
- createdAt, updatedAt

**VendorDocument**
- vendorId (FK)
- type (enum: BUSINESS_LICENSE, TAX_ID, INSURANCE, etc.)
- fileName, mimeType
- content (binary)
- expiryDate
- uploadedBy, uploadedAt

**AuditLog**
- action (enum)
- actorUsername
- vendorId (nullable - for non-vendor actions like login)
- details (max 1000 chars)
- createdAt (immutable)

**AuditFlag**
- vendorId (FK)
- description
- resolved (boolean)
- createdAt, resolvedAt

## Data Flow

### Vendor Onboarding
```
1. Vendor (role: VENDOR) submits form
   ↓
2. POST /api/vendors → VendorController
   ↓
3. VendorService.onboardVendor():
   - Validate registration number uniqueness ✓
   - Create Vendor entity (status = PENDING)
   - Set createdBy = currentUser
   - Save to database
   ↓
4. AuditService.logVendorOnboardingSubmitted()
   - Create AuditLog entry
   ↓
5. Return VendorResponse to client
   ↓
6. Manager sees vendor in dashboard
   - Can approve (PATCH /vendors/{id}/approve)
   - Or reject (PATCH /vendors/{id}/reject)
   ↓
7. VendorService updates status → APPROVED/REJECTED
   ↓
8. AuditService logs approval/rejection event
```

### Risk Calculation Flow
```
Event: Document uploaded
   ↓
1. VendorDocumentService.uploadDocument()
   - Save document to database
   - Trigger: RiskService.recalculateRiskForVendor()
   ↓
2. RiskService.calculateRiskScore():
   - Find all required document types
   - Check latest document per type
   - Count missing documents
   - Count expired documents (expiryDate < today)
   - Count unresolved audit flags
   ↓
3. Calculate score:
   score = MIN(100,
     (missing × 20) +
     (expired × 30) +
     (flags × 25)
   )
   ↓
4. Categorize risk level:
   - LOW: 0-20
   - MEDIUM: 21-50
   - HIGH: 51-100
   ↓
5. VendorService.updateRiskScore()
   - Persist score in Vendor table
   ↓
6. AuditService.logRiskScoreCalculated()
   - Log with details (missing count, expired count)
```

### Scheduled Job Flow
```
Daily 2 AM - Expired Document Check:
   ↓
1. ScheduledJobService.dailyExpiredDocumentCheck()
   ↓
2. Query: SELECT documents WHERE expiryDate <= TODAY
   ↓
3. Group by vendor
   ↓
4. For each vendor with expired docs:
   - Check if "Expired documents" flag already exists
   - If not: Create new AuditFlag
   - Trigger: RiskService.recalculateRiskForVendor()
   ↓
5. Audit trail created automatically

Daily 3 AM - High-Risk Vendor Check:
   ↓
1. ScheduledJobService.dailyHighRiskVendorCheck()
   ↓
2. Query: SELECT vendors WHERE riskScore >= 60
   ↓
3. For each high-risk vendor:
   - Check if "High risk" flag already exists
   - If not: Create new AuditFlag
   ↓
4. Alert flags tracked in database
```

## Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Frontend | React 18 | UI framework |
| Routing | React Router v6 | Client-side routing |
| HTTP Client | Axios | API calls |
| Backend | Spring Boot 3.x | Server framework |
| Security | Spring Security + JWT | Authentication/Authorization |
| ORM | Spring Data JPA | Database abstraction |
| Database | PostgreSQL | Relational database |
| Task Scheduling | Spring @Scheduled | Cron jobs |
| Build | Maven | Dependency/build management |
| Container | Docker | Containerization |
| Deployment | Render/AWS EC2 | Hosting options |

## Design Patterns

**Service Layer Pattern**
- Controllers → Services → Repositories
- Separation of concerns
- Testable business logic

**Repositories Pattern**
- Data access through repositories
- Query abstraction
- Easy to swap implementations

**Interceptor Pattern**
- Axios request/response interceptors
- JWT injection on all requests
- Centralized error handling

**Decorator Pattern**
- `@Transactional` for transaction management
- `@PreAuthorize` for authorization
- `@Scheduled` for periodic tasks

**Singleton Pattern**
- Services are singletons
- Dependency injection manages lifecycle

**Event-Driven Processing**
- Risk recalculation triggered by events
- Audit logging on all state changes
- Scheduled jobs for proactive monitoring

## Security Design

**Authentication:**
- JWT tokens issued on successful login
- Token contains username and roles
- Token stored securely in localStorage
- Auto-logout on token expiration (401)

**Authorization:**
- Role-based access control (RBAC)
- `@PreAuthorize` annotations on endpoints
- Frontend route guards with role checks
- Fallback to login on unauthorized

**Password Security:**
- BCrypt hashing with salt
- Strength validation (8+ chars, numbers, special)
- Never stored/transmitted in plaintext

**Data Security:**
- Parameterized queries (JPA prevents SQL injection)
- Input validation on all endpoints
- HTTPS for all network communication
- Encrypted database connection

**Audit Trail:**
- All actions logged with actor and timestamp
- Immutable audit logs
- Queryable for compliance/investigation

## Scalability Considerations

**Current Design:**
- Single instance deployment
- Shared PostgreSQL database
- No caching layer
- No load balancing

**Scaling Path:**
1. **Horizontal scaling:**
   - Multiple app instances behind load balancer
   - Ensure JWT_SECRET consistent across instances
   - Scheduled jobs need coordination (single instance)

2. **Database scaling:**
   - Read replicas for reporting queries
   - Connection pooling configured
   - Managed database (RDS) handles scaling

3. **Performance optimization:**
   - Database indexing on foreign keys
   - Query optimization
   - Redis caching layer (future)
   - Elasticsearch for audit log search (future)

## Monitoring & Observability

**Current Implementation:**
- Structured logging to files
- Log rotation configured
- Error tracking in logs

**Recommended Additions:**
- APM (New Relic, DataDog)
- Metrics collection (Prometheus)
- Distributed tracing (Jaeger)
- Log aggregation (ELK, CloudWatch)
- Error tracking (Sentry)
- Alerts on anomalies

## Testing Strategy

**Unit Tests:**
- Services tested in isolation
- Mock repositories
- Test business logic rules

**Integration Tests:**
- Full request-response cycle
- Test database interactions
- Test role-based access

**E2E Tests (Frontend):**
- Login flow
- Dashboard access
- Role-based rendering
- API error handling

**Load Testing:**
- Performance baseline
- Concurrent user limits
- Database connection limits

## Deployment Architecture

### Development
```
Local Machine
  ├─ Frontend (npm start, :3000)
  ├─ Backend (mvn spring-boot:run, :8080)
  └─ Database (PostgreSQL local, :5432)
```

### Staging/Testing
```
Docker Environment
  ├─ Frontend (nginx)
  ├─ Backend (Spring Boot in container)
  └─ Database (PostgreSQL in container)
```

### Production
```
Option A - Render
  ├─ Web Service (backend)
  └─ Managed PostgreSQL (database)
  
Option B - AWS
  ├─ EC2 (backend)
  └─ RDS (database)
```

## Configuration Management

**Development**
- application.yml with defaults
- Local PostgreSQL
- JWT secret for local testing

**Production**
- application-prod.yml for profiles
- Environment variables override defaults
- Managed DB credentials from environment
- No sensitive data in code

**Docker**
- Environment variables via docker-compose.yml
- .env file for secrets (gitignored)
- Health checks ensure service readiness

---

**Architecture Last Updated:** February 9, 2026  
**Version:** 1.0  
**Status:** Production Ready
