package it.example.bookshop.order.service.dto;

import java.util.List;

public record CreateOrderRequest(String userId, List<Item> items) {
    public record Item(String isbn, int quantity, Long unitPriceCents) {}
}