package it.example.bookshop.catalog.service.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String name;
    @Column(nullable = false, unique = true)
    public String slug;
}
