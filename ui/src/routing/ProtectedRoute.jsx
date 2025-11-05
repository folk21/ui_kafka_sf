import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";

export default function ProtectedRoute({ role }) {
  const { token, role: currentRole } = useAuth();
  const loc = useLocation();

  if (!token) return <Navigate to="/login" replace state={{ from: loc }} />;

  if (role && currentRole !== role) return <Navigate to="/" replace />;

  return <Outlet />;
}
