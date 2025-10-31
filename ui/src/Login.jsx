import { useState, useEffect } from 'react';
import { useAuth } from './AuthContext';
import { Link, useNavigate } from 'react-router-dom';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [err, setErr] = useState(null);
  const { login } = useAuth();
  const nav = useNavigate();

  const { isAuthenticated } = useAuth();
  useEffect(() => {
    if (isAuthenticated) nav('/welcome');
  }, [isAuthenticated, nav]);

  const submit = async (e) => {
    e.preventDefault(); setErr(null);
    const resp = await fetch('/api/auth/login', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    if (!resp.ok) return setErr('Login failed');
    const data = await resp.json();
    login(data.token, data.username, data.role);
    nav('/welcome');
  };

  return (
    <div className="min-h-[70vh] grid place-items-center">
      <div className="card w-full max-w-md bg-base-100 shadow">
        <div className="card-body">
          <h2 className="card-title">Sign in</h2>
          {err && <div className="alert alert-error py-2 text-sm">{err}</div>}
          <form className="space-y-3" onSubmit={submit}>
            <label className="form-control w-full">
              <div className="label"><span className="label-text">Username</span></div>
              <input className="input input-bordered w-full" value={username} onChange={e => setUsername(e.target.value)} />
            </label>

            <label className="form-control w-full">
              <div className="label"><span className="label-text">Password</span></div>
              <input type="password" className="input input-bordered w-full" value={password} onChange={e => setPassword(e.target.value)} />
            </label>

            <button type="submit" className="btn btn-primary w-full">Sign in</button>
          </form>
          <div className="text-sm opacity-70">
            No account? <Link className="link link-primary" to="/register" onClick={() => {
              setUsername('');
              setPassword('');
              setErr(null);
            }}>Register</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
