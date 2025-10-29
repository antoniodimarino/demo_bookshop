import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthForm from '../components/AuthForm';

function LoginPage() {
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const from = location.state?.from?.pathname || '/';

  const handleLogin = async ({ email, password }) => {
    try {
      setError(null);
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      console.error(err);
      setError('Credenziali non valide. Riprova.');
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <AuthForm mode="login" onSubmit={handleLogin} />
      {error && <p className="error-message">{error}</p>}
      <p>
        Non hai un account? <Link to="/register">Registrati</Link>
      </p>
    </div>
  );
}

export default LoginPage;