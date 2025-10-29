import { useState } from 'react';

function AuthForm({ mode, onSubmit }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  const isRegister = mode === 'register';

  const handleSubmit = (e) => {
    e.preventDefault();
    if (isRegister) {
      onSubmit({ email, password, firstName, lastName });
    } else {
      onSubmit({ email, password });
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {isRegister && (
        <>
          <div>
            <label htmlFor="firstName">Nome</label>
            <input
              id="firstName"
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
            />
          </div>
          <div>
            <label htmlFor="lastName">Cognome</label>
            <input
              id="lastName"
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
            />
          </div>
        </>
      )}
      <div>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
      </div>
      <div>
        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={isRegister ? 8 : 1}
        />
      </div>
      <button type="submit">
        {isRegister ? 'Registrati' : 'Accedi'}
      </button>
    </form>
  );
}

export default AuthForm;