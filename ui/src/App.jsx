import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './AuthContext';
import ProtectedRoute from "./routing/ProtectedRoute";
import Login from './Login';
import Register from './Register';
import Welcome from './Welcome';
import Landing from './Landing';
import AdminUsers from './admin/AdminUsers';
import './index.css';

function Topbar() {
  const { username, role, logout } = useAuth();
  return (
    <div className="navbar bg-base-100 shadow-sm">
      <div className="container mx-auto px-4">
        {role === 'ADMIN' && <Link to="/admin/users" className="btn btn-ghost">Admin</Link>}
        <div className="flex-1">
          <Link to="/" className="btn btn-ghost text-xl">ui_kafka_sf</Link>
        </div>
        <div className="flex-none gap-2">
          {username ? (
            <>
              <div className="badge badge-neutral">{role}</div>
              <div className="hidden sm:block text-sm opacity-80">{username}</div>
              <button className="btn btn-outline btn-sm" onClick={logout}>Logout</button>
            </>
          ) : (
            <>
              <Link className="btn btn-ghost btn-sm" to="/login">Login</Link>
              <Link className="btn btn-primary btn-sm" to="/register">Register</Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Topbar/>
        <div className="container mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<Login/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/register" element={<Register/>}/>
            <Route path="/welcome" element={<Welcome/>}/>
            <Route path="/" element={<Landing/>}/>
            <Route path="/admin/users" element={<AdminUsers/>}/>
            <Route element={<ProtectedRoute role="ADMIN" />}>
              <Route path="/admin/users" element={<AdminUsers />} />
            </Route>          
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
