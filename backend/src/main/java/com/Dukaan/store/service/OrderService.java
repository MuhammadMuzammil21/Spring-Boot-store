package com.Dukaan.store.service;

import com.Dukaan.store.dto.OrderDTO;
import com.Dukaan.store.dto.OrderItemDTO;
import com.Dukaan.store.dto.UserDTO;
import com.Dukaan.store.dto.ProductDTO;
import com.Dukaan.store.model.Order;
import com.Dukaan.store.model.OrderItem;
import com.Dukaan.store.model.User;
import com.Dukaan.store.model.Product;
import com.Dukaan.store.repository.OrderRepository;
import com.Dukaan.store.repository.UserRepository;
import com.Dukaan.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(Order order) {
        double total = 0;
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                throw new RuntimeException("Product not found for order item");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            // Reduce stock
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
            // Calculate price
            total += product.getPrice() * item.getQuantity();
        }
        order.setTotalPrice(total);
        // Save order and items atomically
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // DTO <-> Entity mapping
    public Order toEntity(OrderDTO dto) {
        Order order = new Order();
        if (dto.getUser() != null) {
            User user = userRepository.findByEmail(dto.getUser().getEmail());
            order.setUser(user);
        }
        if (dto.getItems() != null) {
            List<OrderItem> items = dto.getItems().stream().map(this::toEntity).collect(Collectors.toList());
            items.forEach(i -> i.setOrder(order));
            order.setItems(items);
        }
        order.setTotalPrice(dto.getTotal());
        return order;
    }

    public OrderItem toEntity(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setQuantity(dto.getQuantity());
        if (dto.getProduct() != null) {
            // Assume product name is unique for demo; use ID in real app
            Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals(dto.getProduct().getName()))
                .findFirst().orElse(null);
            item.setProduct(product);
        }
        return item;
    }

    public OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setUser(toDTO(order.getUser()));
        dto.setItems(order.getItems().stream().map(this::toDTO).collect(Collectors.toList()));
        dto.setTotal(order.getTotalPrice());
        return dto;
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

    public UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
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
