
import React, { useState } from 'react'
import axios from 'axios'

export default function SfForm({ token }) {
  const [fullName, setFullName] = useState('John Doe')
  const [email, setEmail] = useState('john@example.com')
  const [message, setMessage] = useState('Hello SF!')
  const [status, setStatus] = useState(null)
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await axios.post('/api/sf/submit', { fullName, email, message }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setStatus(data);
    } finally {
      setLoading(false);
    }
  };

  <button type="submit" disabled={loading}>{loading ? 'Sending...' : 'Submit'}</button>

  return (
    <form onSubmit={submit} style={{display:'grid', gap:12, marginTop: 24}}>
      <h2>Salesforce form → Kafka</h2>
      <input value={fullName} onChange={e=>setFullName(e.target.value)} placeholder="Full name"/>
      <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="Email"/>
      <textarea value={message} onChange={e=>setMessage(e.target.value)} placeholder="Message"/>
      <button type="submit">Submit</button>
      {status && <pre>{JSON.stringify(status, null, 2)}</pre>}
    </form>
  )
}
