package it.example.bookshop.inventory.service;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class InventoryItem extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String isbn;
    public int quantity;
    public String location;
}