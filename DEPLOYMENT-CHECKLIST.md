# Deployment Checklist

## Pre-Deployment (Development)

- [ ] All tests passing: `mvn test`
- [ ] Code builds successfully: `mvn clean package`
- [ ] No console errors or warnings
- [ ] Features tested locally
- [ ] Documentation updated
- [ ] Code reviewed
- [ ] Database schema verified

## Database Setup

### Render PostgreSQL
- [ ] Created PostgreSQL instance on Render
- [ ] Database name set to `vendor_compliance`
- [ ] Master user and password configured
- [ ] External connection URL copied
- [ ] Connection tested from local machine

### AWS RDS
- [ ] RDS instance created (db.t2.micro free tier)
- [ ] Engine set to PostgreSQL 15
- [ ] Database name set to `vendor_compliance`
- [ ] Master credentials created
- [ ] Security group allows EC2 access
- [ ] Public accessibility enabled (if needed)
- [ ] Backup enabled
- [ ] Multi-AZ enabled (for production)

### Environment Variables
- [ ] Database URL in `SPRING_DATASOURCE_URL`
- [ ] Database username in `SPRING_DATASOURCE_USERNAME`
- [ ] Database password in `SPRING_DATASOURCE_PASSWORD`
- [ ] JWT secret is 32+ characters
- [ ] JWT secret in `JWT_SECRET`
- [ ] All variables are secure and not in git

## Backend Deployment

### Docker Deployment
- [ ] Docker installed and running
- [ ] Docker Compose installed
- [ ] `.env` file configured with all variables
- [ ] Dockerfile builds successfully
- [ ] `docker-compose up -d` successful
- [ ] `docker logs` show no errors
- [ ] Health check endpoint responding
- [ ] Database connection verified

### Render Deployment
- [ ] GitHub repository created
- [ ] Code pushed to main branch
- [ ] Render account created
- [ ] Web Service created from GitHub
- [ ] Build command: `mvn clean install`
- [ ] Start command: `java -jar target/*.jar`
- [ ] Environment variables added to Render
- [ ] PostgreSQL database created on Render
- [ ] Database URL configured in Render
- [ ] Service deployed successfully
- [ ] Health check passing
- [ ] Logs show no errors

### AWS EC2 Deployment
- [ ] EC2 instance launched (Ubuntu 22.04 LTS)
- [ ] Java 17 installed
- [ ] Maven installed
- [ ] PostgreSQL client installed
- [ ] Application JAR built
- [ ] JAR copied to `/opt/app/`
- [ ] Systemd service created
- [ ] Service enabled with `systemctl enable`
- [ ] Service started successfully
- [ ] Logs show application running
- [ ] Security group configured correctly
- [ ] RDS database accessible from EC2

## Testing Post-Deployment

### Health Checks
- [ ] Application responding to requests
- [ ] Database connection successful
- [ ] Health endpoint: `GET /api/auth/me` (401 expected)
- [ ] No 5xx errors in logs

### Authentication Testing
- [ ] User login successful: `POST /api/auth/login`
- [ ] JWT token received
- [ ] Token works in subsequent requests
- [ ] Token refresh working: `POST /api/auth/refresh`

### API Testing
- [ ] Vendor endpoints functional (GET, POST, PATCH)
- [ ] Document upload working
- [ ] Risk calculation executing
- [ ] Audit logs creating entries
- [ ] Scheduled jobs running (check logs daily)

### Database Testing
- [ ] Tables created correctly
- [ ] Data persisting across restarts
- [ ] Backups configured
- [ ] Queries executing without errors

### Security Testing
- [ ] No sensitive data in logs
- [ ] JWT secret not exposed
- [ ] Database credentials secured
- [ ] HTTPS/TLS enabled
- [ ] Firewall rules restrictive

## Frontend Deployment

### Frontend Build
- [ ] `npm run build` succeeds
- [ ] Build folder created
- [ ] No console errors

### Frontend Server (if self-hosted)
- [ ] Static files served from correct path
- [ ] API URL correctly configured
- [ ] CORS enabled if needed
- [ ] HTTPS active

### Frontend Testing
- [ ] Login page loads
- [ ] Login functionality works
- [ ] JWT token stored in localStorage
- [ ] Dashboard loads after login
- [ ] Logout clears token
- [ ] Role-based redirects working
- [ ] Vendor dashboard accessible to VENDOR role
- [ ] Manager dashboard accessible to VENDOR_MANAGER role
- [ ] Unauthorized access redirects to login

## Monitoring & Maintenance

### Logging
- [ ] Logs configured for production
- [ ] Log rotation enabled
- [ ] Important events being logged
- [ ] Error tracking configured

### Monitoring (Optional but Recommended)
- [ ] Application monitoring enabled (New Relic, DataDog, etc.)
- [ ] Database monitoring enabled
- [ ] Alerts configured for high error rates
- [ ] Performance thresholds set

### Backups
- [ ] Database backups automated
- [ ] Backup retention policy set
- [ ] Backup restoration tested
- [ ] Backup location secured

### Updates
- [ ] Security patches scheduled
- [ ] Dependency updates planned
- [ ] Database version updates planned
- [ ] OS updates scheduled

## Rollback Plan

- [ ] Previous version JAR backed up
- [ ] Database migration script tested
- [ ] Rollback procedure documented
- [ ] Team notified of deployment
- [ ] Rollback command ready if needed

## Post-Deployment

- [ ] Deployment documented in runbooks
- [ ] Team notified of new version
- [ ] Metrics and KPIs monitored
- [ ] User feedback collected
- [ ] Issues logged and tracked
- [ ] Performance baseline established

## Ongoing Checks (Weekly)

- [ ] Application logs reviewed
- [ ] Error rates monitored
- [ ] Database performance checked
- [ ] Backup integrity verified
- [ ] Security patches applied
- [ ] User access audit performed

## Disaster Recovery

- [ ] Database backup tested
- [ ] Application can restore from backup
- [ ] DNS failover configured (if multi-region)
- [ ] RTO (Recovery Time Objective) < 1 hour
- [ ] RPO (Recovery Point Objective) < 1 day

---

## Environment Checklist

| Variable | Value | Status |
|----------|-------|--------|
| SPRING_DATASOURCE_URL | jdbc:postgresql://... | ☐ |
| SPRING_DATASOURCE_USERNAME | postgres | ☐ |
| SPRING_DATASOURCE_PASSWORD | [secure] | ☐ |
| JWT_SECRET | [32+ chars] | ☐ |
| SPRING_PROFILES_ACTIVE | prod | ☐ |
| SPRING_JPA_HIBERNATE_DDL_AUTO | validate | ☐ |
| SERVER_PORT | 8080 | ☐ |

---

## Troubleshooting Quick Reference

### Application Won't Start
- [ ] Check Java version: `java -version`
- [ ] Check database connection
- [ ] Review logs for errors
- [ ] Verify environment variables set

### Database Connection Issues
- [ ] Test connection: `psql -h host -U user -d db`
- [ ] Check firewall/security group rules
- [ ] Verify database is running
- [ ] Check credentials in environment variables

### Frontend Can't Connect to Backend
- [ ] Check API URL in .env
- [ ] Verify backend is running
- [ ] Check CORS configuration
- [ ] Review browser console errors

### JWT Token Issues
- [ ] Verify JWT_SECRET matches between instances
- [ ] Check token expiration setting
- [ ] Ensure consistency across all replicas
- [ ] Test with fresh login

---

**Last Updated:** 2026-02-09
**Deployment Engineer:** _______________
**Date Deployed:** _______________
**Version:** _______________
**Notes:** _______________
