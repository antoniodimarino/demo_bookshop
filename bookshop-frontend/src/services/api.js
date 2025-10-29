import axios from 'axios';

// !!! ATTENZIONE: USA LA ROUTE DEL TUO API-GATEWAY SU OPENSHIFT !!!
// O usa 'http://localhost:8081' se esegui il gateway localmente
const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://api-gateway-antonio-dimarino-dev.apps.rm3.7wse.p1.openshiftapps.com';

class BookshopAPIService {
  constructor(baseURL) {
    this.api = axios.create({
      baseURL: baseURL,
      headers: { 'Content-Type': 'application/json' }
    });
  }

  /**
   * Imposta il token JWT per le richieste future
   * @param {string | null} token
   */
  setAuthToken(token) {
    if (token) {
      this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete this.api.defaults.headers.common['Authorization'];
    }
  }

  // --- Autenticazione ---
  
  async register(email, password, firstName, lastName) {
    const res = await this.api.post('/api/users/register', { email, password, firstName, lastName });
    return res.data;
  }

  async login(email, password) {
    const res = await this.api.post('/api/users/login', { email, password });
    // res.data sarà { token: "..." }
    return res.data;
  }

  // --- Utente (Protetto) ---
  
  async getMe() {
    const res = await this.api.get('/api/users/me');
    // res.data sarà UserDTO
    return res.data;
  }

  // --- Catalogo (Pubblico) ---
  
  async listBooks(search = null, page = 0, size = 20) {
    const res = await this.api.get('/api/books', { params: { search, page, size } });
    return res.data;
  }

  async getBook(isbn) {
    const res = await this.api.get(`/api/books/${isbn}`);
    return res.data;
  }

  // --- Ordini (Protetto) ---
  
  async createOrder(orderRequest) {
    // orderRequest = { userId: '...', items: [...] }
    const res = await this.api.post('/api/orders', orderRequest);
    return res.data;
  }
  
  async createPayment(paymentRequest) {
    // paymentRequest = { orderId: ..., amountCents: ... }
    const res = await this.api.post('/api/payments', paymentRequest);
    return res.data;
  }

  async listMyOrders(page = 0, size = 10) {
    const res = await this.api.get('/api/orders/me', { params: { page, size } });
    return res.data; // Ritorna l'elenco degli ordini
  }

  // --- Admin (Protetto da Ruolo) ---
  
  async createBook(bookData) {
    const res = await this.api.post('/api/books', bookData);
    return res.data;
  }

  async listAllOrders(page = 0, size = 20) {
    const res = await this.api.get('/api/orders', { params: { page, size } });
    return res.data;
  }

  async listAllUsers(page = 0, size = 20) {
    const res = await this.api.get('/api/users', { params: { page, size } });
    return res.data;
  }

  async listInventory(page = 0, size = 50) {
    const res = await this.api.get('/api/inventory', { params: { page, size } });
    return res.data;
  }

  async adjustInventory(isbn, delta, location = null) {
    const res = await this.api.post(`/api/inventory/${isbn}/adjust`, { delta, location });
    return res.data;
  }
}

// Esportiamo una singola istanza (Singleton) del servizio
export const api = new BookshopAPIService(API_BASE_URL);