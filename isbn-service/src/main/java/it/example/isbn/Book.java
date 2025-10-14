package it.example.isbn;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Book extends PanacheEntity {
    @Column(unique = true, nullable = true)
    public String isbn;
    public String title;
    public String author;
}
