package com.Dukaan.store.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "orders") // Because 'Order' is a reserved SQL word
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double totalPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(User user, List<OrderItem> items, double totalPrice) {
        this.user = user;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
