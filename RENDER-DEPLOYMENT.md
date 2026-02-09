# Render Deployment - Step-by-Step

## 🚀 RENDER DEPLOYMENT (Recommended - 2 minutes)

### Prerequisites
✅ GitHub account (you have it)
✅ Render account (free at https://render.com)
✅ Code committed to Git (DONE - see Step 3 below)

---

## Step 1: Create GitHub Repository

1. Go to https://github.com/new
2. **Repository name**: `vendor-compliance`
3. **Description**: "Vendor Compliance Risk Management System"
4. **Visibility**: Public (required for free Render deployment)
5. Click **"Create repository"**

**Copy the pushed command** (it looks like):
```
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/vendor-compliance.git
git push -u origin main
```

---

## Step 2: Push Code to GitHub

In your project directory:

```powershell
cd "c:\Users\SASWAT KUMAR SINGH\OneDrive\Desktop\vendor compliance system"

# Replace YOUR_USERNAME with your actual GitHub username
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/vendor-compliance.git
git push -u origin main
```

**Expected output:**
```
Enumerating objects: 150, done.
Counting objects: 100% (150/150), done.
...
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

✅ **Code is now on GitHub!**

---

## Step 3: Deploy to Render

### 3a. Create PostgreSQL Database (1 minute)

1. Go to https://render.com (sign up if needed)
2. Click **"+ New"** → **"PostgreSQL"**
3. Configure:
   - **Name**: `vendor-db`
   - **Database**: `vendor_compliance`
   - **User**: `postgres`
   - **Region**: Choose closest to you (e.g., Ohio)
   - **PostgreSQL Version**: 15
   - **Plan**: Free
4. Click **"Create Database"**

⏳ **Wait 2-3 minutes for database to be ready**

### 3b: Collect Database Connection Info

After database is created:

1. View database details
2. **Copy these values:**
   - **Internal Database URL**: (looks like `postgresql://postgres:pwd@dpg-xxx.internal:5432/vendor_compliance`)
   - **External Database URL**: (looks like `postgresql://postgres:pwd@dpg-xxx.render.com:5432/vendor_compliance`)
   - **Password**: (shown in page)

Save these in a text file for next step.

### 3c. Create Backend Web Service (2 minutes)

1. Click **"+ New"** → **"Web Service"**
2. **Select Repository**: Choose `vendor-compliance` repository
3. **Connect Repository** (authorize GitHub if prompted)
4. Configure:
   - **Name**: `vendor-compliance-backend`
   - **Environment**: `Java`
   - **Region**: Same as database (Ohio if you chose that)
   - **Branch**: `main`
   - **Build Command**: 
     ```
     mvn clean install -DskipTests
     ```
   - **Start Command**:
     ```
     java -jar target/vendor-compliance-risk-management-system-0.0.1-SNAPSHOT.jar
     ```
   - **Plan**: Free (or Starter if you want reliability)

5. Click **"Create Web Service"** (Don't deploy yet - we need env vars)

### 3d. Add Environment Variables

1. Go to your Web Service → **"Environment"** tab
2. Click **"Add Environment Variable"** and add each:

```
JWT_EXPIRATION=86400000

JWT_SECRET=your_super_secret_key_at_least_32_characters_long_12345

SPRING_DATASOURCE_PASSWORD=<password from 3b>

SPRING_DATASOURCE_URL=<internal URL from 3b (the one with 'internal')>

SPRING_DATASOURCE_USERNAME=postgres

SPRING_JPA_HIBERNATE_DDL_AUTO=validate

SPRING_PROFILES_ACTIVE=prod
```

3. Click **"Save"** after each one

### 3e. Deploy

1. Click **"Deploys"** tab
2. Click **"Deploy latest commit"**
3. Watch the build logs (should take 3-5 minutes)

✅ **When you see "A new render service is live at..."** - You're deployed!

---

## Step 4: Verify Deployment

1. Go to Web Service → **"Settings"**
2. Find **"Render URL"** (looks like `https://vendor-compliance-backend.onrender.com`)
3. Test the API:

```powershell
# Test health check
curl "https://vendor-compliance-backend.onrender.com/api/auth/me"

# Should see 401 Unauthorized (expected - no auth)
# This means the server is running!
```

### Test Login

```powershell
$body = '{"username":"admin","password":"admin123"}'
$headers = @{"Content-Type" = "application/json"}

$response = Invoke-WebRequest `
  -Uri "https://vendor-compliance-backend.onrender.com/api/auth/login" `
  -Method POST `
  -Headers $headers `
  -Body $body

$response.Content | ConvertFrom-Json
```

✅ **Should return JWT token in response**

---

## Step 5: Deploy Frontend (Optional but Recommended)

### Create React Production Build

```powershell
cd "c:\Users\SASWAT KUMAR SINGH\OneDrive\Desktop\vendor compliance system\frontend"
npm install
npm run build
```

This creates `frontend/build/` folder.

### Deploy Frontend to Render

1. Go to Render → **"+ New"** → **"Static Site"**
2. Select `vendor-compliance` repository
3. Configure:
   - **Name**: `vendor-compliance-frontend`
   - **Build Command**: 
     ```
     cd frontend && npm install && npm run build
     ```
   - **Publish Directory**: `frontend/build`
   - **Plan**: Free

4. In **"Environment"** tab, add:
   ```
   REACT_APP_API_URL=https://vendor-compliance-backend.onrender.com/api
   ```

5. Click **"Create Static Site"**

✅ **Frontend will be live in 1-2 minutes**

---

## 🎉 You're Deployed!

**Backend URL**: https://vendor-compliance-backend.onrender.com
**Frontend URL**: https://vendor-compliance-frontend.onrender.com (if deployed)

### Test the Full Application

1. Go to frontend URL
2. Login with:
   - **Username**: `admin`
   - **Password**: `admin123`
3. See vendor list and try approval workflow

---

## 📋 Important Notes

- **Free tier limitations**:
  - Database spins down after 15 min inactivity (5-10 sec cold start)
  - Web service spins down after 30 min inactivity
  - For production, upgrade to Starter ($12/month)

- **First deployment** takes 5-7 minutes due to Maven build
- **Subsequent deployments** are faster (2-3 minutes)

- **Auto-deploy**: Every push to `main` branch automatically deploys

- **View logs**: Go to Web Service → **"Logs"** to debug issues

---

## Troubleshooting

**"Build failed"**
- Check Maven build locally first: `mvn clean install`
- Ensure `application-prod.yml` exists
- Check Build Command is correct

**"Database connection failed"**
- Verify `SPRING_DATASOURCE_URL` is the **internal** URL (with "internal" in it)
- Verify password is correct
- Check database exists on Render

**"Port 8080 not listening"**
- Make sure Start Command ends with `.jar` filename
- Check application starts on `server.port=8080`

**"Frozen after 30 minutes"**
- This is expected on free tier - normal behavior
- Upgrade to Starter plan if you need always-on

---

## Cost Breakdown

| Component | Free Tier | Cost | Notes |
|-----------|-----------|------|-------|
| Web Service | Yes | $0 | Spins down after 30 min |
| PostgreSQL | Yes | $0 | Spins down after 15 min |
| Static Site | Yes | $0 | Frontend |
| **Total** | **Yes** | **$0/month** | **With limitations** |

**Upgrading to Starter:**
| Component | Starter Plan | Cost |
|-----------|----------|------|
| Web Service | Always-on | $7/month |
| PostgreSQL | Always-on | $12/month |
| **Total** | | **$19/month** |

---

**✅ Deployment Complete!** Your system is now live on Render.

Next: Consider deploying to AWS for a more robust production environment.
