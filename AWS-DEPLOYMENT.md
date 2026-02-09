# AWS Deployment - Step-by-Step

## 🚀 AWS DEPLOYMENT (More control, self-managed)

### Prerequisites
✅ AWS account (free tier eligible)
✅ Code on GitHub
✅ Render deployed (or have backend ready)

---

## Architecture Overview

```
┌─────────────────────────────────────────┐
│         Domain / Load Balancer          │
│     (Application Load Balancer)         │
└────────────────┬────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───▼────────┐         ┌──────▼────┐
│  EC2       │         │   EC2      │
│ Instance 1 │         │ Instance 2 │
│ (Optional) │         │ (Optional) │
└───┬────────┘         └──────┬─────┘
    │                         │
    └────────────┬────────────┘
           ┌─────▼──────┐
           │   RDS      │
           │ PostgreSQL │
           │ (Primary)  │
           └────────────┘
```

---

## Step 1: Create AWS Infrastructure

### 1a. Create RDS Database

1. Go to **AWS Console** → Search for **"RDS"**
2. Click **"Create database"**
3. Configure:
   - **Engine**: PostgreSQL
   - **Version**: 15.x
   - **Templates**: Free tier
   - **DB instance identifier**: `vendor-compliance-db`
   - **Master username**: `postgres`
   - **Master password**: `Generate a strong password and save it`
   - **DB instance class**: `db.t3.micro` (free tier)
   - **Storage**: `20 GB` (free tier)
   - **Multi-AZ**: Disable (free tier)
   - **Publicly accessible**: Yes
   - **VPC security group**: Create new → `vendor-compliance-sg`
   - **Initial database name**: `vendor_compliance`

4. Click **"Create database"**

⏳ **Wait 5-10 minutes for RDS database to be available**

### 1b. Configure Security Group (Database)

1. Go to **EC2** → **Security Groups**
2. Find `vendor-compliance-sg`
3. Edit **Inbound rules**:
   - Add rule: Type = PostgreSQL, Protocol = TCP, Port = 5432, Source = 0.0.0.0/0
4. Click **"Save rules"**

### 1c. Create EC2 Security Group (for web tier)

1. Go to **EC2** → **Security Groups**
2. Click **"Create security group"**
3. Configure:
   - **Name**: `vendor-compliance-web`
   - **Description**: "Web server security group"
   - **VPC**: Default
4. Add **Inbound rules**:
   - SSH: Port 22, Source 0.0.0.0/0 (restrict this in production!)
   - HTTP: Port 80, Source 0.0.0.0/0
   - HTTPS: Port 443, Source 0.0.0.0/0
5. Click **"Create security group"**

### 1d. Create EC2 Instance

1. Go to **EC2** → **"Launch instances"**
2. Configure:
   - **Name**: `vendor-compliance-backend`
   - **AMI**: Ubuntu Server 22.04 LTS (free tier)
   - **Instance type**: `t3.micro` (free tier)
   - **Key pair**: 
     - Click **"Create new key pair"**
     - Name: `vendor-compliance-key`
     - Type: RSA
     - Format: .pem
     - Download and save securely!
   - **Security group**: Select `vendor-compliance-web`
   - **Storage**: 30 GB (free tier)
   - **Public IP**: Enable

3. Click **"Launch instance"**

⏳ **Wait 2-3 minutes for instance to start**

---

## Step 2: Get Database Connection Details

1. Go to **RDS** → **Databases**
2. Click `vendor-compliance-db`
3. **Copy these values:**
   - **Endpoint**: (looks like `vendor-compliance-db.xxxxxx.us-east-1.rds.amazonaws.com`)
   - **Master username**: `postgres`
   - **Port**: `5432`

---

## Step 3: Connect to EC2 & Setup Backend

### 3a. SSH into EC2 Instance

1. Go to **EC2** → **Instances**
2. Select `vendor-compliance-backend`
3. Click **"Connect"** → **"EC2 Instance Connect"** (easier) OR
4. Use SSH:
   ```powershell
   # Windows PowerShell
   ssh -i "C:\path\to\vendor-compliance-key.pem" ubuntu@YOUR_EC2_PUBLIC_IP
   ```

   Get Public IP from EC2 console.

### 3b. Update System & Install Java

```bash
sudo apt update
sudo apt upgrade -y

# Install Java 17
sudo apt install openjdk-17-jdk -y

# Verify
java -version
```

### 3c. Install Maven

```bash
sudo apt install maven -y

# Verify
mvn -version
```

### 3d. Install Git

```bash
sudo apt install git -y
```

### 3e. Clone Repository

```bash
cd /home/ubuntu

git clone https://github.com/YOUR_USERNAME/vendor-compliance.git

cd vendor-compliance
```

### 3f. Build Application

```bash
mvn clean install -DskipTests
```

⏳ **First build takes 3-5 minutes**

---

## Step 4: Configure Environment Variables

### 4a. Create .env file

```bash
# Still in /home/ubuntu/vendor-compliance directory

cat > .env << 'EOF'
SPRING_DATASOURCE_URL=jdbc:postgresql://REPLACE_WITH_RDS_ENDPOINT:5432/vendor_compliance
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=REPLACE_WITH_RDS_PASSWORD
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
JWT_SECRET=your_super_secret_jwt_key_at_least_32_chars_long_abc123
JWT_EXPIRATION=86400000
SPRING_PROFILES_ACTIVE=prod
EOF
```

**Edit the file:**
```bash
nano .env  # Edit file
# Paste the above, replace REPLACE_WITH_RDS_* values
# Press Ctrl+X, Y, Enter to save
```

### 4b. Source Environment

```bash
source .env
```

---

## Step 5: Run Application

### Option A: Foreground (for testing)

```bash
cd /home/ubuntu/vendor-compliance
java -jar target/vendor-compliance-risk-management-system-0.0.1-SNAPSHOT.jar
```

**Test it:**
```powershell
# From your local machine
curl "http://YOUR_EC2_PUBLIC_IP:8080/api/auth/login" -X POST `
-H "Content-Type: application/json" `
-d '{"username":"admin","password":"admin123"}'
```

### Option B: Background Service (Recommended)

```bash
# Create systemd service
sudo tee /etc/systemd/system/vendor-compliance.service > /dev/null << 'EOF'
[Unit]
Description=Vendor Compliance Risk Management System
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/vendor-compliance
EnvironmentFile=/home/ubuntu/vendor-compliance/.env
ExecStart=/usr/bin/java -jar target/vendor-compliance-risk-management-system-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start
sudo systemctl enable vendor-compliance
sudo systemctl start vendor-compliance

# Check status
sudo systemctl status vendor-compliance

# View logs
sudo journalctl -u vendor-compliance -f
```

---

## Step 6: Setup Reverse Proxy (Nginx)

### 6a. Install Nginx

```bash
sudo apt install nginx -y
```

### 6b. Configure Nginx

```bash
sudo tee /etc/nginx/sites-available/vendor-compliance > /dev/null << 'EOF'
server {
    listen 80;
    server_name YOUR_EC2_PUBLIC_IP;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/vendor-compliance /etc/nginx/sites-enabled/

# Test nginx config
sudo nginx -t

# Start nginx
sudo systemctl restart nginx
```

### Now access application

```
http://YOUR_EC2_PUBLIC_IP/api/auth/login
```

Instead of `:8080`, traffic goes through nginx on port `80`.

---

## Step 7: Deploy Frontend (Optional)

### Option A: Deploy Frontend to EC2 as Static Site

```bash
cd /home/ubuntu/vendor-compliance/frontend

npm install
npm run build

# Copy to nginx
sudo cp -r build/* /var/www/html/
```

### Option B: Continue Using Render Frontend

Leave frontend on Render, but update API URL:

```javascript
// Update in frontend/.env or code
REACT_APP_API_URL=http://YOUR_EC2_PUBLIC_IP/api
```

Deploy this change to Render → automatically updates.

---

## Step 8: Setup SSL Certificate (HTTPS)

### Use Let's Encrypt with Certbot

```bash
sudo apt install certbot python3-certbot-nginx -y

# Generate certificate
sudo certbot --nginx -d your-domain.com

# Optional: auto-renew
sudo systemctl enable certbot.timer
```

---

## Step 9: Enable Monitoring

### CloudWatch (Built-in)

1. Go to **CloudWatch** console
2. Create **Alarms**:
   - High CPU usage
   - RDS connection failures
   - Disk space low

### Application Monitoring

Add to `application-prod.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      cloudwatch:
        enabled: true
```

---

## 🎉 AWS Deployment Complete!

**Your system is live at:** `http://YOUR_EC2_PUBLIC_IP`

### Cost Breakdown (Monthly)

| Service | Free Tier | Paid | Cost |
|---------|-----------|------|------|
| EC2 (t3.micro) | 750 hrs | Yes | ~$8.50 |
| RDS (db.t3.micro) | 750 hrs | Yes | ~$12 |
| Data Transfer | Some included | Yes | ~$1-10 |
| **Total** | **Until year 1** | **After free tier** | **~$20-30** |

### Upgrade Path

1. **Higher traffic**: Switch to `t3.small` or `t3.medium`
2. **Multi-region**: Add replica database in another region
3. **Load balancing**: Add AWS Application Load Balancer
4. **Auto-scaling**: Setup Auto Scaling Groups
5. **CDN**: Use CloudFront for static assets

---

## Troubleshooting

**"Cannot connect to database"**
```bash
# Test from EC2
psql -h ENDPOINT_ADDRESS -U postgres -d vendor_compliance
```

**"Application not starting"**
```bash
sudo systemctl status vendor-compliance
sudo journalctl -u vendor-compliance -n 50
```

**"Port 8080 already in use"**
```bash
sudo lsof -i :8080
# Kill it: sudo kill -9 PID
```

**"Nginx 502 Bad Gateway"**
```bash
sudo nginx -t
sudo systemctl restart nginx
```

---

## Maintenance Tasks

### Weekly
- Check CloudWatch metrics
- Review logs for errors
- Test API endpoints

### Monthly
- Review RDS backups
- Update OS packages: `sudo apt update && apt upgrade`
- Check disk usage: `df -h`

### Quarterly
- Test disaster recovery (restore from RDS backup)
- Update application code and deploy
- Review security groups

---

**AWS Deployment Ready!**

For production use, consider:
- ✅ Use Route 53 for custom domain
- ✅ Setup CloudFront for CDN
- ✅ Enable RDS automated backups
- ✅ Use Secrets Manager for database credentials
- ✅ Setup VPC with private subnets for RDS

