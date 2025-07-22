package com.Dukaan.store.dto;

public class OrderItemDTO {
    private ProductDTO product;
    private int quantity;
    private double price;

    // Getters and setters
    public ProductDTO getProduct() { return product; }
    public void setProduct(ProductDTO product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
} 