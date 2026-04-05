import { useAuth } from '../state/AuthContext';

export default function DashboardPage() {
  const { auth, profile, loadingProfile } = useAuth();

  return (
    <div className="grid two-cols">
      <section className="card">
        <h2>User Profile</h2>
        {loadingProfile ? <p>Loading profile...</p> : (
          <div className="key-value">
            <div><span>Username</span><strong>{profile?.username}</strong></div>
            <div><span>Email</span><strong>{profile?.email}</strong></div>
            <div><span>Tenant</span><strong>{profile?.tenantCode}</strong></div>
            <div><span>Status</span><strong>{profile?.status}</strong></div>
          </div>
        )}
      </section>
      <section className="card">
        <h2>Token Snapshot</h2>
        <div className="key-value">
          <div><span>Type</span><strong>{auth?.tokenType}</strong></div>
          <div><span>Expires In</span><strong>{auth?.expiresInSeconds}s</strong></div>
          <div><span>Roles</span><strong>{auth?.roles?.join(', ')}</strong></div>
          <div><span>Permissions</span><strong>{auth?.permissions?.join(', ')}</strong></div>
        </div>
      </section>
      <section className="card wide">
        <h2>Access Token</h2>
        <textarea readOnly value={auth?.accessToken || ''} rows={8} />
      </section>
      <section className="card wide">
        <h2>Refresh Token</h2>
        <textarea readOnly value={auth?.refreshToken || ''} rows={6} />
      </section>
    </div>
  );
}
