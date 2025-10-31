import { Link } from 'react-router-dom';

export default function Landing() {
  return (
    <div className="min-h-[70vh] grid place-items-center">
      <div className="card bg-base-100 shadow w-full max-w-lg">
        <div className="card-body items-center text-center">
          <h1 className="text-2xl font-semibold">Welcome to ui_kafka_sf</h1>
          <p className="opacity-70">Please choose an option:</p>
          <div className="card-actions mt-4">
            <Link className="btn btn-primary" to="/login">Login</Link>
            <Link className="btn btn-outline" to="/register">Register</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
