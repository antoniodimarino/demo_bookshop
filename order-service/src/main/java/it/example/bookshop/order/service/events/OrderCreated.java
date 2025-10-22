package it.example.bookshop.order.service.events;

import java.util.List;

public record OrderCreated(Long orderId, String userId, List<Item> items, Long totalCents) {
    public record Item(String isbn, int quantity, Long unitPriceCents) {}
}