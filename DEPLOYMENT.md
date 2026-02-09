# Deployment Guide

## Backend Deployment Options

Choose one of the following platforms:

### Option 1: Render (Recommended for simplicity)

#### Prerequisites
- GitHub account with repo pushed
- Render account (https://render.com)

#### Step 1: Push to GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/vendor-compliance.git
git push -u origin main
```

#### Step 2: Create Render Web Service

1. Go to https://render.com
2. Click "New Web Service"
3. Connect GitHub repository
4. Select the repository
5. Configure:
   - **Name**: vendor-compliance-backend
   - **Environment**: Java
   - **Build Command**: `mvn clean install`
   - **Start Command**: `java -jar target/vendor-compliance-risk-management-system-0.0.1-SNAPSHOT.jar`
   - **Instance Type**: Starter (free tier) or Standard

#### Step 3: Set Environment Variables

Click "Environment" and add:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://pg-xxx.render.com:5432/vendor_compliance
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_db_password
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
JWT_SECRET=your_jwt_secret_key_min_32_chars
JWT_EXPIRATION=86400000
SPRING_PROFILES_ACTIVE=prod
```

#### Step 4: Connect Database

In Render dashboard:
1. Create PostgreSQL database
2. Copy connection details
3. Update `SPRING_DATASOURCE_URL` in environment variables

#### Cost
- **Free Tier**: Starter Web Service + PostgreSQL (1 GB)
- **Paid**: ~$12/month for reliable service

#### Deploy
Push to GitHub and Render auto-deploys from main branch.

---

### Option 2: AWS EC2

#### Prerequisites
- AWS account
- EC2 micro instance (free tier eligible)
- SSH key pair

#### Step 1: Launch EC2 Instance

1. AWS Console → EC2 → Launch Instance
2. Select: **Ubuntu Server 22.04 LTS**
3. Instance Type: **t2.micro** (free tier)
4. Network: Default VPC
5. Storage: 30 GB (free tier)
6. Security Group:
   - SSH: 22 (0.0.0.0/0)
   - HTTP: 80 → 8080
   - HTTPS: 443 → 8080

#### Step 2: Connect and Setup

```bash
# SSH into instance
ssh -i your-key.pem ec2-user@your-instance-ip

# Update system
sudo apt update
sudo apt upgrade -y

# Install Java
sudo apt install openjdk-17-jdk-headless -y

# Install Maven
sudo apt install maven -y

# Install PostgreSQL client
sudo apt install postgresql-client -y
```

#### Step 3: Deploy JAR

```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/vendor-compliance.git
cd vendor-compliance

# Build JAR
mvn clean package -DskipTests

# Move to /opt
sudo mkdir -p /opt/app
sudo mv target/*.jar /opt/app/app.jar
sudo chown ec2-user:ec2-user /opt/app/app.jar
```

#### Step 4: Create Systemd Service

```bash
sudo nano /etc/systemd/system/vendor-compliance.service
```

Add:
```ini
[Unit]
Description=Vendor Compliance Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/app
ExecStart=/usr/bin/java -jar /opt/app/app.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

Environment="SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-url:5432/vendor_compliance"
Environment="SPRING_DATASOURCE_USERNAME=postgres"
Environment="SPRING_DATASOURCE_PASSWORD=your_password"
Environment="JWT_SECRET=your_jwt_secret_key"
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

#### Step 5: Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable vendor-compliance
sudo systemctl start vendor-compliance
sudo systemctl status vendor-compliance
```

#### Step 6: Setup RDS Database

1. AWS Console → RDS → Create database
2. Engine: PostgreSQL
3. DB instance: db.t2.micro (free tier)
4. DB name: vendor_compliance
5. Master username: postgres
6. Master password: (generate strong password)
7. Connectivity: Public (for now), allow EC2 security group
8. Copy endpoint and update connection string

#### Cost
- **Free Tier**: EC2 micro + RDS micro (12 months)
- **After free tier**: ~$20-30/month

---

## Database Setup

### Managed Options

#### Option A: Render PostgreSQL (Recommended)

1. Render Dashboard → Create PostgreSQL
2. Plan: Free (2 GB, auto-pause)
3. Database name: `vendor_compliance`
4. External connection string provided
5. Copy full URL to `SPRING_DATASOURCE_URL`

#### Option B: AWS RDS

1. AWS Console → RDS → Create database
2. Engine: PostgreSQL 15
3. Free tier eligible instance
4. Enable public accessibility
5. Create security group inbound rule for EC2
6. Copy endpoint to `SPRING_DATASOURCE_URL`

#### Option C: Supabase (PostgreSQL SaaS)

1. Sign up at https://supabase.com
2. Create project
3. Copy connection string
4. Free tier: 500 MB storage, 2 projects

### Connection String Format

```
jdbc:postgresql://hostname:5432/database_name
```

### Initialize Database

```bash
# Using psql (if installed)
psql -h hostname -U postgres -d vendor_compliance -f schema.sql

# Or Spring will auto-create tables with:
spring.jpa.hibernate.ddl-auto=create
```

---

## Environment Variables

### Production (.env or system environment)

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/vendor_compliance
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_secure_password_min_16_chars
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# JPA/Hibernate
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=false

# JWT Security
JWT_SECRET=your_jwt_secret_key_minimum_32_characters_long_random_string
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# Application Profile
SPRING_PROFILES_ACTIVE=prod

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_VENDORCOMPLIANCE=INFO

# Scheduling
SPRING_TASK_SCHEDULING_POOL_SIZE=2
SPRING_TASK_EXECUTION_POOL_CORE_SIZE=2
```

### How to Set Environment Variables

#### Render
- Web Service → Environment tab → Add variables

#### AWS EC2 with Systemd
- Edit `/etc/systemd/system/vendor-compliance.service`
- Add `Environment="KEY=value"` lines

#### Docker
- Use `docker run -e KEY=value`
- Or `.env` file with `docker-compose`

#### Local Development
- Create `.env.local` file (gitignored)
- Use `@Value("${JWT_SECRET}")` in code

---

## Docker Deployment

### Dockerfile

```dockerfile
# Build stage
FROM maven:3.8-openjdk-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.JarLauncher || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: vendor_compliance
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: your_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/vendor_compliance
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: your_password
      JWT_SECRET: your_jwt_secret_key_min_32_chars
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    restart: unless-stopped

volumes:
  postgres_data:
```

### Deploy with Docker

```bash
# Build image
docker build -t vendor-compliance .

# Run with Compose
docker-compose up -d

# Check logs
docker-compose logs -f app

# Stop
docker-compose down
```

---

## CI/CD Pipeline (GitHub Actions)

### .github/workflows/deploy.yml

```yaml
name: Deploy to Render

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Run tests
        run: mvn test
      
      - name: Deploy to Render
        if: success()
        run: |
          curl https://api.render.com/deploy/srv-${{ secrets.RENDER_DEPLOY_SERVICE_ID }}?key=${{ secrets.RENDER_DEPLOY_KEY }}
```

### Setup GitHub Secrets

1. GitHub Repo → Settings → Secrets and variables
2. Add:
   - `RENDER_DEPLOY_SERVICE_ID`: Your Render service ID
   - `RENDER_DEPLOY_KEY`: Your Render deployment key

---

## Monitoring & Logs

### Render
- Dashboard → Logs page
- Real-time log streaming
- Error tracking

### AWS EC2
```bash
# View service logs
sudo journalctl -u vendor-compliance -f

# Check service status
sudo systemctl status vendor-compliance

# View application logs (if redirected)
tail -f /var/log/vendor-compliance/app.log
```

### Docker
```bash
docker-compose logs -f app
docker logs --tail 100 container_name
```

---

## Domain & HTTPS

### Render (Auto)
- Free domain: serviceXXXX.onrender.com
- Auto SSL/TLS with Let's Encrypt

### AWS EC2
```bash
# Install Let's Encrypt
sudo apt install certbot python3-certbot-nginx -y

# Get certificate
sudo certbot certonly --standalone -d your-domain.com

# Configure Nginx
sudo apt install nginx -y
# Configure as reverse proxy to :8080
```

---

## Security Checklist

✅ Environment variables for sensitive data  
✅ Database credentials in secrets  
✅ JWT secret is 32+ characters  
✅ HTTPS/TLS enabled  
✅ Firewall rules restrict access  
✅ Database is not public (if possible)  
✅ Auto-backup enabled (RDS)  
✅ Monitoring and alerts configured  
✅ Regular security updates  

---

## Cost Summary

| Service | Option | Free Tier | Paid |
|---------|--------|-----------|------|
| Compute | Render | ✅ Starter | $12/mo |
| Compute | AWS EC2 | ✅ 12 months | $10/mo |
| Database | Render PostgreSQL | ✅ 2GB | $20/mo |
| Database | AWS RDS | ✅ 12 months | $20/mo |
| **Total** | **Render** | **$0 (limited)** | **~$32/mo** |
| **Total** | **AWS (after free)** | **$0 (12mo)** | **~$30/mo** |

---

## Troubleshooting

### Backend won't start
```bash
# Check Java version
java -version

# Check logs
docker logs app_name

# Test database connection
postgres --version
psql -h db_host -U postgres -d vendor_compliance -c "SELECT 1"
```

### High latency
- Check database location matches region
- Enable query caching
- Use connection pooling (HikariCP)

### Database connection errors
- Verify connection string format
- Check security group/firewall rules
- Confirm database is running
- Test with psql client

### JWT errors
- Verify JWT_SECRET is 32+ characters
- Check expiration time setting
- Ensure consistent secret across instances

---

## Production Readiness Checklist

Before deploying to production:

- [ ] Update `application.yml` for prod profile
- [ ] Set all environment variables
- [ ] Configure database backups
- [ ] Setup monitoring/alerts
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Run security tests
- [ ] Load test the application
- [ ] Setup log aggregation
- [ ] Document runbook for team
- [ ] Schedule database maintenance
- [ ] Setup auto-scaling (if applicable)
