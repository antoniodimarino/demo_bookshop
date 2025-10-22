package it.example.bookshop.payment.service.dto;

public record PaymentRequest(Long orderId, Long amountCents, String method, String token) {
}