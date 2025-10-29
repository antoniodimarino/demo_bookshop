import { useState, useEffect } from 'react';
import { api } from '../services/api';

function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [orders, setOrders] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [error, setError] = useState(null);

  // Carica dati admin
  const loadAdminData = async () => {
    try {
      setError(null);
      const [usersData, ordersData, invData] = await Promise.all([
        api.listAllUsers(0, 10),
        api.listAllOrders(0, 10),
        api.listInventory(0, 50)
      ]);
      setUsers(usersData);
      setOrders(ordersData);
      setInventory(invData);
    } catch (err) {
      console.error(err);
      setError('Impossibile caricare i dati admin.');
    }
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  // Gestore per il form di creazione libro
  const handleCreateBook = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const bookData = {
      isbn: formData.get('isbn'),
      title: formData.get('title'),
      authors: formData.get('authors').split(','),
      categories: formData.get('categories').split(','),
      priceCents: parseInt(formData.get('priceCents'), 10),
      publishedYear: parseInt(formData.get('publishedYear'), 10),
      description: formData.get('description'),
      language: formData.get('language'),
    };

    try {
      await api.createBook(bookData);
      alert('Libro creato con successo!');
      e.target.reset();
    } catch (err) {
      console.error(err);
      alert(`Errore creazione libro: ${err.message}`);
    }
  };

  const handleAdjustInventory = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const isbn = formData.get('isbn');
    const delta = parseInt(formData.get('delta'), 10);
    const location = formData.get('location') || null;

    if (!isbn || !delta) {
      alert('ISBN e Delta sono obbligatori');
      return;
    }

    try {
      await api.adjustInventory(isbn, delta, location);
      alert('Inventario aggiornato!');
      e.target.reset();
      loadAdminData();
    } catch (err) {
      console.error(err);
      alert(`Errore aggiornamento: ${err.message}`);
    }
  };

  return (
    <div className="admin-panel">
      <h2>Pannello di Amministrazione</h2>
      {error && <p className="error-message">{error}</p>}
      
      <hr />
      
      <h3>Gestione Inventario (POST /api/inventory/../adjust)</h3>
      <form onSubmit={handleAdjustInventory}>
        <div><label>ISBN:</label><input name="isbn" type="text" required /></div>
        <div><label>Delta (es. 5 o -5):</label><input name="delta" type="number" required /></div>
        <div><label>Nuova Location (opz.):</label><input name="location" type="text" /></div>
        <button type="submit">Aggiorna Stock</button>
      </form>

      <h3>Inventario Attuale (GET /api/inventory)</h3>
      <ul>
        {inventory.map(item => (
          <li key={item.isbn}>
            <strong>{item.isbn}</strong> - Qty: {item.quantity} (Pos: {item.location || 'N/A'})
          </li>
        ))}
      </ul>

      <h3>Crea Nuovo Libro (POST /api/books)</h3>
      <form onSubmit={handleCreateBook}>
        {/* Form semplificato */}
        <div><label>ISBN:</label><input name="isbn" type="text" required /></div>
        <div><label>Titolo:</label><input name="title" type="text" required /></div>
        <div><label>Autori (separati da virgola):</label><input name="authors" type="text" /></div>
        <div><label>Categorie (separati da virgola):</label><input name="categories" type="text" /></div>
        <div><label>Prezzo (in centesimi):</label><input name="priceCents" type="number" /></div>
        <div><label>Anno:</label><input name="publishedYear" type="number" /></div>
        <div><label>Lingua:</label><input name="language" type="text" defaultValue="it" /></div>
        <div><label>Descrizione:</label><textarea name="description" /></div>
        <button type="submit">Crea Libro</button>
      </form>

      <hr />
      
      <h3>Ultimi Utenti (GET /api/users)</h3>
      <ul>
        {users.map(user => (
          <li key={user.id}>{user.email} ({user.firstName} {user.lastName}) - {user.role}</li>
        ))}
      </ul>
      
      <hr />

      <h3>Ultimi Ordini (GET /api/orders)</h3>
      <ul>
        {orders.map(order => (
          <li key={order.id}>Ordine #{order.id} - Stato: {order.status} (Utente: {order.userId})</li>
        ))}
      </ul>
    </div>
  );
}

export default AdminDashboard;