import axios from 'axios';
export const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use(cfg => {
  const t = localStorage.getItem('token');
  if (t) cfg.headers.Authorization = `Bearer ${t}`;
  return cfg;
});

api.interceptors.response.use(
  r => r,
  err => {
    if (err.response && (err.response.status === 401 || err.response.status === 403)) {
      console.warn('Access denied', err.response.status, err.response.data);
    }
    return Promise.reject(err);
  }
);

