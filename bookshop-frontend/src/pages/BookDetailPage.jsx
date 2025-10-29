import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../services/api';
import { useCart } from '../hooks/useCart';

const formatPrice = (cents) => {
  if (!cents) return 'N/A';
  return (cents / 100).toLocaleString('it-IT', {
    style: 'currency',
    currency: 'EUR',
  });
};

function BookDetailPage() {
  const { isbn } = useParams();
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCart();

  useEffect(() => {
    const fetchBook = async () => {
      try {
        setLoading(true);
        const data = await api.getBook(isbn);
        setBook(data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchBook();
  }, [isbn]);

  if (loading) return <div>Caricamento...</div>;
  if (!book) return <h2>Libro non trovato</h2>;

  const handleAddToCart = () => {
    addToCart(book, 1);
    alert(`${book.title} aggiunto al carrello!`);
  };

  return (
    <div className="book-detail">
      <img 
        src={`https://via.placeholder.com/200x300.png?text=${book.title}`} 
        alt={book.title} 
      />
      <h1>{book.title}</h1>
      <h3>di {book.authors?.join(', ') || 'N/A'}</h3>
      <p className="price">{formatPrice(book.priceCents)}</p>
      <p>{book.description || 'Nessuna descrizione.'}</p>
      <p>
        <strong>ISBN:</strong> {book.isbn}<br />
        <strong>Anno:</strong> {book.publishedYear || 'N/A'}<br />
        <strong>Lingua:</strong> {book.language || 'N/A'}<br />
      </p>
      <button onClick={handleAddToCart}>
        Aggiungi al Carrello
      </button>
    </div>
  );
}

export default BookDetailPage;