package com.Dukaan.store.service;

import com.Dukaan.store.dto.OrderItemDTO;
import com.Dukaan.store.dto.ProductDTO;
import com.Dukaan.store.model.OrderItem;
import com.Dukaan.store.model.Product;
import com.Dukaan.store.repository.OrderItemRepository;
import com.Dukaan.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id).orElse(null);
    }

    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public void deleteOrderItem(Long id) {
        orderItemRepository.deleteById(id);
    }

    // DTO <-> Entity mapping
    public OrderItem toEntity(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setQuantity(dto.getQuantity());
        if (dto.getProduct() != null) {
            Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals(dto.getProduct().getName()))
                .findFirst().orElse(null);
            item.setProduct(product);
        }
        return item;
    }

    public OrderItemDTO toDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getProduct() != null ? item.getProduct().getPrice() : 0);
        if (item.getProduct() != null) {
            dto.setProduct(toDTO(item.getProduct()));
        }
        return dto;
    }

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        return dto;
    }
}
