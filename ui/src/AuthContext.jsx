import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import axios from "axios";

const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

export function AuthProvider({ children }) {
  // Base state sourced from localStorage on first render
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [username, setUsername] = useState(() => localStorage.getItem("username"));
  const [role, setRole] = useState(() => localStorage.getItem("role"));

  // Initialize axios Authorization header from persisted token on mount
  useEffect(() => {
    if (token) {
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    } else {
      delete axios.defaults.headers.common["Authorization"];
    }
  }, [token]);

  // Derived helpers
  const isAuthenticated = !!token;
  const hasRole = (r) => role === r;

  // Login: persist auth, set axios header
  const login = (t, u, r) => {
    setToken(t);
    setUsername(u);
    setRole(r);
    localStorage.setItem("token", t);
    localStorage.setItem("username", u);
    localStorage.setItem("role", r);
    axios.defaults.headers.common["Authorization"] = `Bearer ${t}`;
  };

  // Logout: clear auth, remove axios header
  const logout = () => {
    setToken(null);
    setUsername(null);
    setRole(null);
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    delete axios.defaults.headers.common["Authorization"];
  };

  const value = useMemo(
    () => ({ token, username, role, isAuthenticated, hasRole, login, logout }),
    [token, username, role]
  );

  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}
