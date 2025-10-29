import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';

function Header() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const { cartItems } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const cartItemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <nav>
      <NavLink to="/">Home (Catalogo)</NavLink>
      {isAdmin && <NavLink to="/admin">Dashboard Admin</NavLink>}
      <div className="spacer"></div>

      <NavLink to="/cart">
        Carrello ({cartItemCount})
      </NavLink>
      
      <div className="auth-links">
        {isAuthenticated ? (
          <>
            <NavLink to="/profile">Ciao, {user.firstName}</NavLink>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <NavLink to="/login">Login</NavLink>
            <NavLink to="/register">Registrati</NavLink>
          </>
        )}
      </div>
    </nav>
  );
}

export default Header;