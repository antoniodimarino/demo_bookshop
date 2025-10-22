package it.example.isbn;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class Book extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String isbn;
    public String title;
    public String author;
}