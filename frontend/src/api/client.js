import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
});

api.interceptors.request.use((config) => {
  const auth = JSON.parse(localStorage.getItem('uas-auth') || '{}');
  if (auth?.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`;
  }
  if (auth?.tenantCode) {
    config.headers['X-Tenant-ID'] = auth.tenantCode;
  }
  return config;
});

export default api;
