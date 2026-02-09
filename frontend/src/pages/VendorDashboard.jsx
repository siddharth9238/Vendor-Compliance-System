import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import axiosInstance from '../api/axios';
import '../styles/Dashboard.css';

export const VendorDashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchVendors = async () => {
      try {
        const response = await axiosInstance.get('/vendors');
        setVendors(response.data);
      } catch (err) {
        setError('Failed to load vendors');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchVendors();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div>
          <h1>Vendor Dashboard</h1>
          <p>Welcome, {user?.fullName || user?.username}</p>
        </div>
        <button onClick={handleLogout} className="logout-btn">
          Logout
        </button>
      </header>

      <main className="dashboard-content">
        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="loading">Loading vendors...</div>
        ) : (
          <div className="vendors-grid">
            <h2>Your Vendors</h2>
            {vendors.length === 0 ? (
              <p className="no-data">No vendors found</p>
            ) : (
              <table className="vendors-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Legal Name</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Risk Score</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {vendors.map((vendor) => (
                    <tr key={vendor.id}>
                      <td>{vendor.id}</td>
                      <td>{vendor.legalName}</td>
                      <td>{vendor.email}</td>
                      <td>
                        <span className={`status-badge status-${vendor.status.toLowerCase()}`}>
                          {vendor.status}
                        </span>
                      </td>
                      <td>
                        <span className={`risk-score risk-${getRiskLevel(vendor.riskScore)}`}>
                          {vendor.riskScore}/100
                        </span>
                      </td>
                      <td>{new Date(vendor.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </main>
    </div>
  );
};

function getRiskLevel(score) {
  if (score <= 20) return 'low';
  if (score <= 50) return 'medium';
  return 'high';
}
