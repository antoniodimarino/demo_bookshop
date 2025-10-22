package it.example.bookshop.common.dto;

public record AddressDTO(Long id, String label, String street, String city, String country, String zip, boolean primaryAddress) {}