import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthForm from '../components/AuthForm';

function RegisterPage() {
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();
  const { register } = useAuth();

  const handleRegister = async (formData) => {
    try {
      setError(null);
      setSuccess(false);
      await register(formData.email, formData.password, formData.firstName, formData.lastName);
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      console.error(err);
      if (err.message.includes('409')) {
        setError('Email già in uso.');
      } else {
        setError('Errore durante la registrazione.');
      }
    }
  };

  if (success) {
    return (
      <div>
        <h2>Registrazione completata!</h2>
        <p>Stai per essere reindirizzato alla pagina di login...</p>
        <p>Nel caso in cui la pagina non dovesse caricarsi automaticamente, puoi effettuare il login  
        <Link to="/login">cliccando qui</Link>
        </p>
      </div>
    );
  }

  return (
    <div>
      <h2>Registrati</h2>
      <AuthForm mode="register" onSubmit={handleRegister} />
      {error && <p className="error-message">{error}</p>}
      <p>
        Hai già un account? <Link to="/login">Accedi</Link>
      </p>
    </div>
  );
}

export default RegisterPage;