package com.Dukaan.store.controller;

import com.Dukaan.store.dto.OrderItemDTO;
import com.Dukaan.store.model.OrderItem;
import com.Dukaan.store.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public List<OrderItemDTO> getAllOrderItems() {
        return orderItemService.getAllOrderItems().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public OrderItemDTO createOrderItem(@RequestBody OrderItemDTO orderItemDTO) {
        OrderItem orderItem = toEntity(orderItemDTO);
        OrderItem saved = orderItemService.createOrderItem(orderItem);
        return toDTO(saved);
    }

    @GetMapping("/{id}")
    public OrderItemDTO getOrderItemById(@PathVariable Long id) {
        OrderItem orderItem = orderItemService.getOrderItemById(id);
        return orderItem != null ? toDTO(orderItem) : null;
    }

    @DeleteMapping("/{id}")
    public void deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
    }

    private OrderItemDTO toDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getProduct() != null ? item.getProduct().getPrice() : 0);
        // Product mapping omitted for brevity
        return dto;
    }

    private OrderItem toEntity(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setQuantity(dto.getQuantity());
        // Product and order should be set in service layer for real use
        return item;
    }
}
