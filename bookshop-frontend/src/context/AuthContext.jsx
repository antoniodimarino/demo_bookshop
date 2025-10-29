import { useState, useEffect } from 'react';
import { api } from '../services/api';

import { AuthContext } from './auth-context';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem('bookshop_token'));
  const [isLoading, setIsLoading] = useState(true);

  // Effetto: Se abbiamo un token salvato, impostiamolo in Axios
  // e proviamo a caricare i dati dell'utente.
  useEffect(() => {
    async function loadUserFromToken() {
      if (token) {
        api.setAuthToken(token);
        try {
          const userData = await api.getMe();
          setUser(userData);
        } catch (error) {
          console.error("Token non valido o scaduto, logout in corso. " + error);
          logout();
        }
      }
      setIsLoading(false);
    }
    loadUserFromToken();
  }, [token]);

  const login = async (email, password) => {
    // 1. Ottieni il token
    const { token } = await api.login(email, password);
    
    // 2. Salva il token
    localStorage.setItem('bookshop_token', token);
    api.setAuthToken(token);
    setToken(token);

    // 3. Ottieni i dati utente
    const userData = await api.getMe();
    setUser(userData);
  };

  const register = async (email, password, firstName, lastName) => {
    // La registrazione NON fa il login automatico nel nostro backend
    // In un'app reale, potresti voler chiamare login() subito dopo.
    await api.register(email, password, firstName, lastName);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('bookshop_token');
    api.setAuthToken(null);
  };

  const isAuthenticated = !!user;
  const isAdmin = user?.role === 'ADMIN';

  // Forniamo lo stato e le funzioni al resto dell'app
  const value = {
    user,
    token,
    isAuthenticated,
    isAdmin,
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}