package it.example.bookshop.order.service;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class CustomerOrder extends PanacheEntity {

    @Column(nullable = false)
    public String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public OrderStatus status = OrderStatus.NEW;

    /** Somma degli item in centesimi (opzionale se non gestisci prezzi) */
    public Long totalCents;

    @Column(nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem it) {
        it.order = this;
        this.items.add(it);
    }
}