import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { useAuth } from '../hooks/useAuth';
import { api } from '../services/api';

// Helper per il prezzo
const formatPrice = (cents) => {
  if (!cents && cents !== 0) return 'N/A';
  return (cents / 100).toLocaleString('it-IT', {
    style: 'currency',
    currency: 'EUR',
  });
};

function CartPage() {
  const { cartItems, removeFromCart, totalPriceCents, clearCart } = useCart();
  const { user } = useAuth(); // Ci serve l'ID utente per l'ordine
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleCheckout = async () => {
    if (!user) {
      setError("Devi essere loggato per procedere.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // 1. Prepara la richiesta d'ordine
      const orderRequest = {
        // L'ID utente deve essere una stringa come da DTO del backend
        userId: String(user.id), 
        items: cartItems.map(item => ({
          isbn: item.book.isbn,
          quantity: item.quantity,
          unitPriceCents: item.book.priceCents
        }))
      };

      // 2. Chiama l'API del gateway per creare l'ordine
      // La risposta contiene l'ordine creato, incluso l'ID e il totale
      const createdOrder = await api.createOrder(orderRequest);

      // 3. (Mock) Chiama l'API per il pagamento
      // Usiamo l'ID e il totale calcolato dal backend
      const paymentRequest = {
        orderId: createdOrder.id,
        amountCents: createdOrder.totalCents,
        method: "MOCK_PAYMENT_GATEWAY", // In un caso reale, qui ci sarebbe un token (Stripe/PayPal)
        token: "mock-token-12345"
      };

      await api.createPayment(paymentRequest);

      // 4. Successo!
      alert('Ordine e pagamento completati con successo!');
      clearCart();
      navigate('/profile'); // Reindirizza l'utente al suo profilo

    } catch (err) {
      console.error(err);
      setError(`Errore durante il checkout: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  if (cartItems.length === 0) {
    return <h2>Il tuo carrello è vuoto.</h2>;
  }

  return (
    <div className="cart-page" style={{ textAlign: 'left' }}>
      <h2>Carrello</h2>
      {cartItems.map(item => (
        <div key={item.book.isbn} style={{ borderBottom: '1px solid #444', padding: '1rem 0' }}>
          <h4>{item.book.title}</h4>
          <p>Quantità: {item.quantity}</p>
          <p>Prezzo: {formatPrice(item.book.priceCents * item.quantity)}</p>
          <button onClick={() => removeFromCart(item.book.isbn)}>Rimuovi</button>
        </div>
      ))}
      <hr />
      <h3>Totale: {formatPrice(totalPriceCents)}</h3>
      
      {error && <p className="error-message">{error}</p>}
      
      <button onClick={handleCheckout} disabled={loading}>
        {loading ? 'Elaborazione...' : 'Procedi al Pagamento'}
      </button>
    </div>
  );
}

export default CartPage;