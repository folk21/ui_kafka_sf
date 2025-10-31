import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function Register() {
  const [username,setUsername]=useState('');
  const [password,setPassword]=useState('');
  const [role,setRole]=useState('STUDENT');
  const [msg,setMsg]=useState(null);
  const [err,setErr]=useState(null);
  const nav = useNavigate();

  const submit = async (e) => {
    e.preventDefault(); setErr(null); setMsg(null);
    const resp = await fetch('/api/auth/register', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password, role }),
    });
    const ok = resp.ok; const data = await resp.json().catch(()=>({}));
    if (!ok) return setErr(data?.error || 'Registration failed');
    setMsg('Registered. You can sign in now.');
    setTimeout(()=>nav('/login'), 700);
  };

  return (
    <div className="min-h-[70vh] grid place-items-center">
      <div className="card w-full max-w-md bg-base-100 shadow">
        <div className="card-body">
          <h2 className="card-title">Create account</h2>
          {msg && <div className="alert alert-success py-2 text-sm">{msg}</div>}
          {err && <div className="alert alert-error py-2 text-sm">{err}</div>}

          <form className="space-y-3" onSubmit={submit}>
            <label className="form-control">
              <div className="label"><span className="label-text">Username</span></div>
              <input className="input input-bordered" value={username} onChange={e=>setUsername(e.target.value)} />
            </label>

            <label className="form-control">
              <div className="label"><span className="label-text">Password</span></div>
              <input type="password" className="input input-bordered" value={password} onChange={e=>setPassword(e.target.value)} />
            </label>

            <label className="form-control">
              <div className="label"><span className="label-text">Role</span></div>
              <select className="select select-bordered" value={role} onChange={e=>setRole(e.target.value)}>
                <option value="STUDENT">STUDENT</option>
                <option value="INSTRUCTOR">INSTRUCTOR</option>
              </select>
            </label>

            <button className="btn btn-primary w-full" type="submit">Register</button>
          </form>

          <div className="text-sm opacity-70">
            Already have an account? <Link className="link link-primary" to="/login">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
