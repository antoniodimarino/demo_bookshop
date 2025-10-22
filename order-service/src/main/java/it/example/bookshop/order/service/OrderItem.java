package it.example.bookshop.order.service;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    public CustomerOrder order;

    @Column(nullable = false)
    public String isbn;

    @Column(nullable = false)
    public int quantity;

    /** Prezzo unitario in centesimi (facoltativo) */
    public Long unitPriceCents;
}