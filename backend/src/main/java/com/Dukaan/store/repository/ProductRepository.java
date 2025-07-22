package com.Dukaan.store.repository;

import com.Dukaan.store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // You can add custom queries later if needed
}
