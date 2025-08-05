package com.Dukaan.store.service;

import com.Dukaan.store.model.Product;
import com.Dukaan.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String name, Double minPrice, Double maxPrice) {
        List<Product> allProducts = productRepository.findAll();
        
        return allProducts.stream()
                .filter(product -> {
                    boolean matches = true;
                    
                    if (name != null && !name.trim().isEmpty()) {
                        matches = matches && (product.getName().toLowerCase().contains(name.toLowerCase()) ||
                                            product.getDescription().toLowerCase().contains(name.toLowerCase()));
                    }
                    
                    if (minPrice != null) {
                        matches = matches && product.getPrice() >= minPrice;
                    }
                    
                    if (maxPrice != null) {
                        matches = matches && product.getPrice() <= maxPrice;
                    }
                    
                    return matches;
                })
                .collect(Collectors.toList());
    }

    public Page<Product> getProductsPaginated(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}
