import { Link } from 'react-router-dom';
import { useCart } from '../hooks/useCart';

// Helper per formattare il prezzo
const formatPrice = (cents) => {
  if (!cents) return 'N/A';
  return (cents / 100).toLocaleString('it-IT', {
    style: 'currency',
    currency: 'EUR',
  });
};

function BookCard({ book }) {
  const { addToCart } = useCart();

  const handleAddToCart = (e) => {
    // Impedisce alla card di navigare quando si clicca il bottone
    e.preventDefault(); 
    addToCart(book, 1);
    alert(`${book.title} aggiunto al carrello!`);
  };

  return (
    <article className="book-card">
      <Link to={`/books/${book.isbn}`}>
        {/* Immagine segnaposto */}
        <img 
          src={`https://via.placeholder.com/150x220.png?text=${book.title}`} 
          alt={book.title} 
          style={{ width: '100%', borderRadius: '4px' }}
        />
        <h3>{book.title}</h3>
      </Link>
      <p>{book.authors?.join(', ') || 'Autore Sconosciuto'}</p>
      <strong>{formatPrice(book.priceCents)}</strong>
      <button 
        onClick={handleAddToCart} 
        style={{ width: '100%', marginTop: '0.5rem' }}
      >
        Aggiungi al Carrello
      </button>
    </article>
  );
}

export default BookCard;