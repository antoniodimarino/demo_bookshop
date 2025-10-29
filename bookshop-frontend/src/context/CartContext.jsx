import { useState } from 'react';

import { CartContext } from './cart-context';

export function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState([]);

  // Aggiunge un libro (o incrementa la quantità)
  const addToCart = (book, quantity = 1) => {
    setCartItems((prevItems) => {
      const existingItem = prevItems.find(item => item.book.isbn === book.isbn);

      if (existingItem) {
        // Incrementa la quantità
        return prevItems.map(item =>
          item.book.isbn === book.isbn
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        // Aggiunge nuovo item
        return [...prevItems, { book, quantity }];
      }
    });
  };

  // Rimuove un libro dal carrello
  const removeFromCart = (isbn) => {
    setCartItems((prevItems) => prevItems.filter(item => item.book.isbn !== isbn));
  };

  // Svuota il carrello (dopo il checkout)
  const clearCart = () => {
    setCartItems([]);
  };

  // Calcola il totale
  const totalPriceCents = cartItems.reduce(
    (total, item) => total + (item.book.priceCents * item.quantity),
    0
  );

  const value = {
    cartItems,
    addToCart,
    removeFromCart,
    clearCart,
    totalPriceCents,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}