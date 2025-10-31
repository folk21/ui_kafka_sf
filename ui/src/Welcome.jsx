import { useEffect } from 'react';
import { useAuth } from './AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Welcome() {
  const { username, role, logout } = useAuth();
  const nav = useNavigate();

  useEffect(() => { if (!username) nav('/login'); }, [username, nav]);
  const icon = role === 'ADMIN' ? '👑' : role === 'INSTRUCTOR' ? '🧑‍🏫' : '🎓';

  return (
    <div className="min-h-[60vh] grid place-items-center">
      <div className="card bg-base-100 shadow w-full max-w-lg">
        <div className="card-body items-center text-center">
          <div className="text-6xl">{icon}</div>
          <h1 className="text-2xl font-semibold">Welcome, {username}!</h1>
          <p className="opacity-70">Your role is <span className="font-mono">{role}</span>.</p>
          <div className="card-actions pt-2">
            <button className="btn btn-outline" onClick={()=>{ logout(); nav('/login'); }}>Log out</button>
          </div>
        </div>
      </div>
    </div>
  );
}
