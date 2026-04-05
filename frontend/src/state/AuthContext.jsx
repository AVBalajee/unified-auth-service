import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import api from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem('uas-auth');
    return saved ? JSON.parse(saved) : null;
  });
  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(false);

  useEffect(() => {
    if (auth) {
      localStorage.setItem('uas-auth', JSON.stringify(auth));
    } else {
      localStorage.removeItem('uas-auth');
    }
  }, [auth]);

  useEffect(() => {
    if (auth?.accessToken) {
      fetchProfile();
    } else {
      setProfile(null);
    }
  }, [auth?.accessToken]);

  async function login(payload) {
    const response = await api.post('/api/auth/login', payload, {
      headers: { 'X-Tenant-ID': payload.tenantCode }
    });
    localStorage.setItem('uas-auth', JSON.stringify(response.data));
    setAuth(response.data);
    return response.data;
  }

  async function refreshToken() {
    if (!auth?.refreshToken || !auth?.tenantCode) {
      throw new Error('Missing refresh token');
    }
    const response = await api.post('/api/auth/refresh', {
      refreshToken: auth.refreshToken,
      tenantCode: auth.tenantCode
    }, {
      headers: { 'X-Tenant-ID': auth.tenantCode }
    });
    localStorage.setItem('uas-auth', JSON.stringify(response.data));
    setAuth(response.data);
    return response.data;
  }

  async function logout() {
    try {
      if (auth?.accessToken && auth?.refreshToken) {
        await api.post('/api/auth/logout', { refreshToken: auth.refreshToken }, {
          headers: {
            Authorization: `Bearer ${auth.accessToken}`,
            'X-Tenant-ID': auth.tenantCode
          }
        });
      }
    } finally {
      setAuth(null);
      setProfile(null);
    }
  }

  async function fetchProfile() {
    try {
      setLoadingProfile(true);
      const response = await api.get('/api/auth/me');
      setProfile(response.data);
    } catch (error) {
      if (auth?.refreshToken) {
        try {
          await refreshToken();
          const retried = await api.get('/api/auth/me');
          setProfile(retried.data);
        } catch {
          setAuth(null);
          setProfile(null);
        }
      } else {
        setAuth(null);
        setProfile(null);
      }
    } finally {
      setLoadingProfile(false);
    }
  }

  const value = useMemo(() => ({ auth, profile, loadingProfile, login, logout, refreshToken, fetchProfile, setAuth }), [auth, profile, loadingProfile]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
