package it.example.bookshop.common.dto;

public record RegisterRequest(String email, String password, String firstName, String lastName) {}