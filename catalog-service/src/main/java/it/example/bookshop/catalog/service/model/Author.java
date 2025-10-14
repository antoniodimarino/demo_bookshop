package it.example.bookshop.catalog.service.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "authors")
public class Author extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String name;
    @Column(length = 2000)
    public String bio;
}
