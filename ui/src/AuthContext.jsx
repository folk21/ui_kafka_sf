import { createContext, useContext, useState } from 'react';

const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [username, setUsername] = useState(localStorage.getItem('username'));
  const [role, setRole] = useState(localStorage.getItem('role'));

  const login = (t, u, r) => {
    setToken(t); setUsername(u); setRole(r);
    localStorage.setItem('token', t);
    localStorage.setItem('username', u);
    localStorage.setItem('role', r);
  };
  const logout = () => {
    setToken(null); setUsername(null); setRole(null);
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
  };

  return <AuthCtx.Provider value={{ token, username, role, login, logout }}>{children}</AuthCtx.Provider>;
}
