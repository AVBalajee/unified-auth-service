import { useEffect, useState } from 'react';
import api from '../api/client';
import { useAuth } from '../state/AuthContext';

export default function UsersPage() {
  const { auth } = useAuth();
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    tenantCode: auth?.tenantCode || 'BANK_A',
    roleNames: []
  });

  async function loadData() {
    setError('');
    try {
      const [usersRes, rolesRes] = await Promise.all([
        api.get('/api/users'),
        api.get('/api/roles')
      ]);
      setUsers(usersRes.data);
      setRoles(rolesRes.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to load admin data. Your user may not have enough permissions.');
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  async function createUser(event) {
    event.preventDefault();
    setMessage('');
    setError('');
    try {
      await api.post('/api/users', form);
      setMessage('User created successfully');
      setForm({ username: '', email: '', password: '', tenantCode: auth?.tenantCode || 'BANK_A', roleNames: [] });
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create user');
    }
  }

  function toggleRole(roleName) {
    setForm((prev) => ({
      ...prev,
      roleNames: prev.roleNames.includes(roleName)
        ? prev.roleNames.filter((value) => value !== roleName)
        : [...prev.roleNames, roleName]
    }));
  }

  return (
    <div className="grid two-cols">
      <section className="card">
        <h2>Create User</h2>
        {message && <div className="alert success">{message}</div>}
        {error && <div className="alert error">{error}</div>}
        <form onSubmit={createUser} className="stack gap-sm">
          <label>Username</label>
          <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
          <label>Email</label>
          <input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <label>Password</label>
          <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          <label>Tenant</label>
          <input value={form.tenantCode} onChange={(e) => setForm({ ...form, tenantCode: e.target.value.toUpperCase() })} />
          <label>Roles</label>
          <div className="checkbox-grid">
            {roles.map((role) => (
              <label key={role.id} className="checkbox-item">
                <input type="checkbox" checked={form.roleNames.includes(role.roleName)} onChange={() => toggleRole(role.roleName)} />
                <span>{role.roleName}</span>
              </label>
            ))}
          </div>
          <button className="primary-btn">Create User</button>
        </form>
      </section>
      <section className="card">
        <h2>Users in Current Tenant</h2>
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Status</th>
                <th>Roles</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.status}</td>
                  <td>{user.roles.join(', ')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
