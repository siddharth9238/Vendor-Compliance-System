import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/LoginPage';
import { VendorDashboard } from './pages/VendorDashboard';
import { ManagerDashboard } from './pages/ManagerDashboard';
import { ProtectedRoute } from './components/ProtectedRoute';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route
          path="/vendor/dashboard"
          element={
            <ProtectedRoute requiredRole="VENDOR">
              <VendorDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/manager/dashboard"
          element={
            <ProtectedRoute requiredRole="VENDOR_MANAGER">
              <ManagerDashboard />
            </ProtectedRoute>
          }
        />

        <Route path="/unauthorized" element={<UnauthorizedPage />} />
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

const UnauthorizedPage = () => (
  <div className="error-page">
    <h1>Access Denied</h1>
    <p>You do not have permission to access this resource.</p>
    <a href="/login">Return to Login</a>
  </div>
);

export default App;
