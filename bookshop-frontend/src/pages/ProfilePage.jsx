import { useAuth } from '../hooks/useAuth';
import { useState, useEffect } from 'react';
import { api } from '../services/api';

const formatPrice = (cents) => {
  if (!cents && cents !== 0) return 'N/A';
  return (cents / 100).toLocaleString('it-IT', {
    style: 'currency',
    currency: 'EUR',
  });
};

const formatDate = (isoString) => {
  if (!isoString) return 'N/A';
  return new Date(isoString).toLocaleString('it-IT', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
};

function ProfilePage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await api.listMyOrders(0, 20); // Carica i primi 20 ordini
        setOrders(data);
      } catch (err) {
        console.error(err);
        setError('Impossibile caricare lo storico ordini.');
      } finally {
        setLoading(false);
      }
    };

    // Esegui il fetch solo se l'utente è caricato
    if (user) {
      fetchOrders();
    }
  }, [user]); // Ricarica se l'utente cambia

  if (!user) {
    return <div>Caricamento dati utente...</div>;
  }

  return (
    <div className="profile-page">
      <h2>Il mio Profilo</h2>
      <p>
        <strong>Nome:</strong> {user.firstName} {user.lastName}
      </p>
      <p>
        <strong>Email:</strong> {user.email}
      </p>
      <p>
        <strong>Ruolo:</strong> {user.role}
      </p>
      <p>
        <strong>ID Utente:</strong> {user.id}
      </p>

      <hr style={{ margin: '2rem 0', borderColor: '#555' }} />

      {/* --- Sezione Storico Ordini --- */}
      <h3>I miei Ordini</h3>
      
      {loading && <p>Caricamento ordini in corso...</p>}
      
      {error && <p className="error-message">{error}</p>}

      {!loading && !error && orders.length === 0 && (
        <p>Non hai ancora effettuato nessun ordine.</p>
      )}

      {orders.length > 0 && (
        <div className="order-list">
          {orders.map(order => (
            // Applica uno stile per separare gli ordini
            <div key={order.id} className="order-summary" style={{ 
                border: '1px solid #444', 
                padding: '1rem', 
                marginBottom: '1rem', 
                borderRadius: '8px',
                background: '#2c2c2c'
              }}>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <h4>Ordine #{order.id}</h4>
                <strong style={{ fontSize: '1.1em' }}>{order.status}</strong>
              </div>
              
              <p>Data: {formatDate(order.createdAt)}</p>
              <p>Totale: <strong>{formatPrice(order.totalCents)}</strong></p>
              
              <h5 style={{ marginTop: '1rem', borderBottom: '1px solid #444', paddingBottom: '0.25rem' }}>
                Articoli:
              </h5>
              <ul style={{ listStyle: 'none', paddingLeft: '0.5rem' }}>
                {order.items.map(item => (
                  <li key={item.id} style={{ marginBottom: '0.25rem' }}>
                    {item.quantity}x (ISBN: {item.isbn}) - {formatPrice(item.unitPriceCents * item.quantity)}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}
      {/* --- Fine Sezione Ordini --- */}
    </div>
  );
}

export default ProfilePage;