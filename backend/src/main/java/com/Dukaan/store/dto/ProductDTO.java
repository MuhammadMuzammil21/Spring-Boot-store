package com.Dukaan.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Product data transfer object")
public class ProductDTO {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Schema(description = "Product name", example = "Smartphone", required = true)
    private String name;
    
    @NotBlank(message = "Product description is required")
    @Size(min = 10, max = 500, message = "Product description must be between 10 and 500 characters")
    @Schema(description = "Product description", example = "Latest Android smartphone with 128GB storage", required = true)
    private String description;
    
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Product price must be less than 1,000,000")
    @Schema(description = "Product price", example = "299.99", required = true)
    private double price;
    
    @NotNull(message = "Product stock is required")
    @Min(value = 0, message = "Product stock cannot be negative")
    @Max(value = 999999, message = "Product stock must be less than 1,000,000")
    @Schema(description = "Product stock quantity", example = "50", required = true)
    private int stock;

    // Default constructor
    public ProductDTO() {}

    // Constructor with all fields
    public ProductDTO(String name, String description, double price, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
