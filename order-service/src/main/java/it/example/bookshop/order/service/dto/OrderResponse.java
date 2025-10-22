package it.example.bookshop.order.service.dto;

import it.example.bookshop.order.service.CustomerOrder;
import it.example.bookshop.order.service.OrderItem;
import it.example.bookshop.order.service.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String userId,
        OrderStatus status,
        Long totalCents,
        Instant createdAt,
        List<Item> items
) {
    public record Item(Long id, String isbn, int quantity, Long unitPriceCents) {}

    public static OrderResponse fromEntity(CustomerOrder o) {
        List<Item> items = o.items.stream()
                .map(i -> new Item(i.id, i.isbn, i.quantity, i.unitPriceCents))
                .toList();
        return new OrderResponse(o.id, o.userId, o.status, o.totalCents, o.createdAt, items);
    }
}