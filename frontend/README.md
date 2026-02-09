# Vendor Compliance Frontend

Minimal React frontend for the Vendor Compliance Risk Management System.

## Features

- JWT-based authentication
- Secure token management (localStorage)
- Role-based route protection
- Axios interceptors for token injection
- Automatic logout on 401 response
- Vendor and Manager dashboards

## Setup

### Prerequisites
- Node.js (v14+)
- npm or yarn

### Installation

```bash
cd frontend
npm install
```

### Configuration

Update `.env` file with your API URL:

```env
REACT_APP_API_URL=http://localhost:8080/api
```

### Running the App

```bash
npm start
```

The app will open at `http://localhost:3000`

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── api/
│   │   └── axios.js           # Axios setup with JWT interceptor
│   ├── hooks/
│   │   └── useAuth.js         # Authentication hook
│   ├── components/
│   │   └── ProtectedRoute.jsx # Route protection component
│   ├── pages/
│   │   ├── LoginPage.jsx      # Login page
│   │   ├── VendorDashboard.jsx
│   │   └── ManagerDashboard.jsx
│   ├── styles/
│   │   ├── LoginPage.css
│   │   └── Dashboard.css
│   ├── App.jsx                # Main app with routing
│   ├── index.js               # React entry point
│   └── index.css              # Global styles
├── .env                       # Environment variables
├── .gitignore
└── package.json
```

## Key Components

### useAuth Hook

Custom hook for authentication state management:

```javascript
const { user, token, isAuthenticated, login, logout, hasRole } = useAuth();
```

### Axios Interceptor

Request interceptor adds JWT token:
```javascript
config.headers.Authorization = `Bearer ${token}`;
```

Response interceptor handles 401:
```javascript
if (error.response?.status === 401) {
  // Clear storage and redirect to login
}
```

### ProtectedRoute Component

Wraps routes requiring authentication:

```javascript
<ProtectedRoute requiredRole="VENDOR">
  <VendorDashboard />
</ProtectedRoute>
```

## Demo Credentials

```
Vendor User:
  Username: vendor1
  Password: Password1!

Manager User:
  Username: manager1
  Password: Password1!
```

## Security Features

✅ JWT tokens stored securely in localStorage
✅ Automatic token injection on all API calls
✅ Automatic logout on token expiration (401)
✅ Role-based route protection
✅ Redirect to login on unauthorized access

## API Integration

All API calls use the configured Axios instance:

```javascript
// GET request with automatic JWT injection
const response = await axiosInstance.get('/vendors');

// POST request with automatic JWT injection
const response = await axiosInstance.post('/vendors', vendorData);
```

## Building for Production

```bash
npm run build
```

Creates optimized production build in `build/` folder.

## Notes

This is a minimal frontend focused on integration. For production:
- Add more comprehensive error handling
- Implement proper form validation
- Add loading states
- Implement refresh token rotation
- Add tests
- Minify CSS/JS
