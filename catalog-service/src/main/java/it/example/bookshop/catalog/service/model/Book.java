package it.example.bookshop.catalog.service.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.Instant;

import java.util.*;


@Entity
@Table(name = "books")
public class Book extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String isbn;
    @Column(nullable = false)
    public String title;
    @Column(length = 2000)
    public String description;
    public String language;
    public Integer publishedYear;
    public Long priceCents;
    @Column(nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    @ManyToMany
    @JoinTable(name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    public Set<Author> authors = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    public Set<Category> categories = new HashSet<>();
}
