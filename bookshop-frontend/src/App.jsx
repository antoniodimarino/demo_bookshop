import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import HomePage from './pages/HomePage';
import BookDetailPage from './pages/BookDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import AdminDashboard from './pages/AdminDashboard';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import { useAuth } from './hooks/useAuth';
import CartPage from './pages/CartPage';

function App() {
  const { isLoading } = useAuth();

  // Non mostrare nulla finché non abbiamo verificato il token
  if (isLoading) {
    return <div>Caricamento in corso...</div>;
  }

  return (
    <>
      <Header />
      <main>
        <Routes>
          {/* Rotte Pubbliche */}
          <Route path="/" element={<HomePage />} />
          <Route path="/books/:isbn" element={<BookDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Rotte Protette (Solo Utenti Loggati) */}
          <Route element={<ProtectedRoute />}>
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/cart" element={<CartPage />} />
            {/* Aggiungi qui altre rotte protette, es. /checkout */}
          </Route>
          
          {/* Rotte Admin (Solo Ruolo ADMIN) */}
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<AdminDashboard />} />
          </Route>
          
          {/* Rotta Catch-All */}
          <Route path="*" element={<h2>404 - Pagina Non Trovata</h2>} />
        </Routes>
      </main>
    </>
  );
}

export default App;