package it.example.bookshop.user.service.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "addresses")
public class Address extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    public User user;
    public String label; // es. "Home", "Office"
    @Column(nullable = false)
    public String street;
    @Column(nullable = false)
    public String city;
    @Column(nullable = false)
    public String country;
    public String zip;
    public boolean primaryAddress;
}
