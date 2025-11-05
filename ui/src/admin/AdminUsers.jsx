import { useEffect, useState } from 'react';
import { api } from '../api';

export default function AdminUsers() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pwdFor, setPwdFor] = useState(null); // username для смены пароля
  const [pwd, setPwd] = useState('');

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
      <h1 className="text-2xl font-semibold">Users</h1>

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
