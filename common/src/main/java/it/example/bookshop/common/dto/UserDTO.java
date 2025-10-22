package it.example.bookshop.common.dto;

import java.util.List;

// Usato per le risposte, non contiene dati sensibili come la password
public record UserDTO(Long id, String email, String firstName, String lastName, String role, List<AddressDTO> addresses) {}