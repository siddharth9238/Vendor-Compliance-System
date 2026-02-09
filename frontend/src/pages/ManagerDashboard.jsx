import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import axiosInstance from '../api/axios';
import '../styles/Dashboard.css';

export const ManagerDashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('');

  useEffect(() => {
    const fetchVendors = async () => {
      try {
        const url = filter ? `/vendors?status=${filter}` : '/vendors';
        const response = await axiosInstance.get(url);
        setVendors(response.data);
      } catch (err) {
        setError('Failed to load vendors');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchVendors();
  }, [filter]);

  const handleApprove = async (vendorId) => {
    try {
      await axiosInstance.patch(`/vendors/${vendorId}/approve`, {
        comments: 'Approved by manager',
      });
      setVendors(vendors.map((v) =>
        v.id === vendorId ? { ...v, status: 'APPROVED' } : v
      ));
    } catch (err) {
      alert('Failed to approve vendor: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div>
          <h1>Manager Dashboard</h1>
          <p>Welcome, {user?.fullName || user?.username}</p>
        </div>
        <button onClick={handleLogout} className="logout-btn">
          Logout
        </button>
      </header>

      <main className="dashboard-content">
        <div className="controls">
          <label htmlFor="status-filter">Filter by Status:</label>
          <select
            id="status-filter"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          >
            <option value="">All</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>

        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="loading">Loading vendors...</div>
        ) : (
          <div className="vendors-grid">
            <h2>Vendor Management</h2>
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
                    <th>Actions</th>
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
                      <td>
                        {vendor.status === 'PENDING' && (
                          <button
                            className="action-btn approve-btn"
                            onClick={() => handleApprove(vendor.id)}
                          >
                            Approve
                          </button>
                        )}
                      </td>
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
