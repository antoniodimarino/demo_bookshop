package it.example.bookshop.order.service.events;

import java.util.List;

public record ReserveStockEvent(Long orderId, String userId, List<Item> items) {
    public record Item(String isbn, int quantity, String location) {}
}