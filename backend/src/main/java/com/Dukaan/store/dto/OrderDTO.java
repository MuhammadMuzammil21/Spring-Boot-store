
package com.Dukaan.store.dto;

import java.util.List;

public class OrderDTO {
    private UserDTO user;
    private List<OrderItemDTO> items;
    private double total;

    // Getters and setters
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
} 