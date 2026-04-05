import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../state/AuthContext';

export default function LoginPage() {
  const { login, auth } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '', tenantCode: 'BANK_A' });
  const [tenants, setTenants] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (auth?.accessToken) {
      navigate('/');
    }
  }, [auth, navigate]);

  useEffect(() => {
    api.get('/api/tenants/public').then((response) => setTenants(response.data)).catch(() => {});
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>Unified Auth Service</h1>
        <p className="muted">Tenant-aware authentication with JWT, RBAC, OAuth2 client credentials, and Redis.</p>
        {error && <div className="alert error">{error}</div>}
        <label>Tenant</label>
        <select value={form.tenantCode} onChange={(e) => setForm({ ...form, tenantCode: e.target.value })}>
          {tenants.map((tenant) => (
            <option key={tenant.id} value={tenant.tenantCode}>{tenant.tenantCode} - {tenant.tenantName}</option>
          ))}
        </select>
        <label>Username</label>
        <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} placeholder="Enter username" />
        <label>Password</label>
        <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="Enter password" />
        <button className="primary-btn" disabled={loading}>{loading ? 'Signing in...' : 'Login'}</button>
        <div className="demo-box">
          <strong>Demo users</strong>
          <ul>
            <li>platformadmin / Admin@123 / PLATFORM</li>
            <li>tradeadmin / Admin@123 / BANK_A</li>
            <li>opsuser / Admin@123 / BANK_A</li>
            <li>viewerb / Admin@123 / BANK_B</li>
          </ul>
        </div>
      </form>
    </div>
  );
}
