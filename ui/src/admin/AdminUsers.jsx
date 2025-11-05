import { useEffect, useState } from 'react';
import { api } from '../api';
import { useAuth } from "../AuthContext"; // проверь путь!
import { useNavigate } from "react-router-dom";

export default function AdminUsers() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pwdFor, setPwdFor] = useState(null);
  const [pwd, setPwd] = useState('');

  const { token, logout } = useAuth();   // <— добавили token
  const navigate = useNavigate();

  // если токен пропал → уходим на /login и компонент размонтируется
  useEffect(() => {
    if (!token) {
      navigate("/login", { replace: true });
    }
  }, [token, navigate]);

  const onLogout = () => {
    logout();
    // navigate("/login", { replace: true }); // уже не обязательно — эффект выше сработает
  };

  async function load() {
    setLoading(true);
    try {
      const r = await api.get('/admin/users');
      setItems(r.data);
    } catch (e) {
      console.error(e);
      alert(`Cannot load users: ${e?.response?.status || e}`);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => { load(); }, []);

  async function changePassword() {
    if (!pwdFor || !pwd) return;
    await api.put(`/admin/users/${encodeURIComponent(pwdFor)}/password`, { newPassword: pwd });
    setPwd(''); setPwdFor(null);
    alert('Password updated');
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Users</h1>
        <button className="btn" onClick={onLogout}>Logout</button>
      </div>

      {loading ? <div className="loading loading-spinner loading-md"></div> : (
        <div className="overflow-x-auto">
          <table className="table">
            <thead>
              <tr><th>Username</th><th>Role</th><th></th></tr>
            </thead>
            <tbody>
              {items.map(u => (
                <tr key={u.username}>
                  <td>{u.username}</td>
                  <td>{u.role}</td>
                  <td>
                    <button className="btn btn-sm"
                            onClick={() => setPwdFor(u.username)}>
                      Change password
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {pwdFor && (
        <div className="p-4 rounded-box border space-y-3 max-w-md">
          <div className="font-medium">Change password: {pwdFor}</div>
          <input
            className="input input-bordered w-full"
            type="password"
            placeholder="New password"
            value={pwd}
            onChange={e => setPwd(e.target.value)}
          />
          <div className="flex gap-2">
            <button className="btn btn-primary" onClick={changePassword}>Save</button>
            <button className="btn" onClick={() => { setPwdFor(null); setPwd(''); }}>Cancel</button>
          </div>
        </div>
      )}
    </div>
  );
}
