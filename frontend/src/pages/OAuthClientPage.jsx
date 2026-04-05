import { useState } from 'react';
import api from '../api/client';

export default function OAuthClientPage() {
  const [form, setForm] = useState({
    clientId: 'trade-service',
    clientSecret: 'trade-secret',
    grantType: 'client_credentials',
    tenantCode: 'BANK_A',
    scope: 'read write'
  });
  const [tokenResponse, setTokenResponse] = useState(null);
  const [error, setError] = useState('');

  async function getToken(event) {
    event.preventDefault();
    setError('');
    setTokenResponse(null);
    try {
      const response = await api.post('/api/oauth2/token', form, {
        headers: { 'X-Tenant-ID': form.tenantCode }
      });
      setTokenResponse(response.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to issue client token');
    }
  }

  return (
    <div className="grid two-cols">
      <section className="card">
        <h2>OAuth2 Client Credentials</h2>
        <form onSubmit={getToken} className="stack gap-sm">
          <label>Tenant</label>
          <input value={form.tenantCode} onChange={(e) => setForm({ ...form, tenantCode: e.target.value.toUpperCase() })} />
          <label>Client ID</label>
          <input value={form.clientId} onChange={(e) => setForm({ ...form, clientId: e.target.value })} />
          <label>Client Secret</label>
          <input type="password" value={form.clientSecret} onChange={(e) => setForm({ ...form, clientSecret: e.target.value })} />
          <label>Grant Type</label>
          <input value={form.grantType} onChange={(e) => setForm({ ...form, grantType: e.target.value })} />
          <label>Scope</label>
          <input value={form.scope} onChange={(e) => setForm({ ...form, scope: e.target.value })} />
          <button className="primary-btn">Issue Token</button>
        </form>
        {error && <div className="alert error">{error}</div>}
      </section>
      <section className="card">
        <h2>Machine Token Output</h2>
        {tokenResponse ? (
          <>
            <div className="key-value">
              <div><span>Client ID</span><strong>{tokenResponse.clientId}</strong></div>
              <div><span>Tenant</span><strong>{tokenResponse.tenantCode}</strong></div>
              <div><span>Scope</span><strong>{tokenResponse.scope}</strong></div>
              <div><span>Expires In</span><strong>{tokenResponse.expiresInSeconds}s</strong></div>
            </div>
            <textarea rows={12} readOnly value={tokenResponse.accessToken} />
          </>
        ) : <p className="muted">Issue a token to test service-to-service authentication.</p>}
      </section>
    </div>
  );
}
