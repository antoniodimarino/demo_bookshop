import { useState, useEffect } from 'react';
import { api } from '../services/api';
import BookCard from '../components/BookCard';

function HomePage() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchBooks = async () => {
      try {
        setLoading(true);
        const data = await api.listBooks(null, 0, 20); // Carica i primi 20
        setBooks(data);
        setError(null);
      } catch (err) {
        setError('Impossibile caricare i libri.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchBooks();
  }, []);

  return (
    <div>
      <h2>Catalogo Libri</h2>
      {loading && <div>Caricamento...</div>}
      {error && <div className="error-message">{error}</div>}
      <div className="book-list">
        {books.map((book) => (
          <BookCard key={book.isbn} book={book} />
        ))}
      </div>
    </div>
  );
}

export default HomePage;