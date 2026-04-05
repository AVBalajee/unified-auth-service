import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../state/AuthContext';

export default function Layout() {
  const { auth, profile, logout } = useAuth();
  const location = useLocation();

  const links = [
    { to: '/', label: 'Dashboard' },
    { to: '/users', label: 'Users' },
    { to: '/oauth-client', label: 'OAuth Client' }
  ];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h2>Unified Auth</h2>
        <p className="muted">Tenant: {auth?.tenantCode || '-'}</p>
        <nav>
          {links.map((link) => (
            <Link key={link.to} to={link.to} className={location.pathname === link.to ? 'nav-link active' : 'nav-link'}>
              {link.label}
            </Link>
          ))}
        </nav>
        <button className="secondary-btn" onClick={logout}>Logout</button>
      </aside>
      <main className="content">
        <header className="topbar">
          <div>
            <strong>{profile?.username || auth?.username}</strong>
            <div className="muted small">{profile?.email}</div>
          </div>
          <div className="pill">{auth?.tenantCode}</div>
        </header>
        <Outlet />
      </main>
    </div>
  );
}
