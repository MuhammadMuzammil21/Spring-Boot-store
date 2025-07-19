package com.Dukaan.store.controller;

import com.Dukaan.store.model.OrderItem;
import com.Dukaan.store.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemSerOrderItemService;

    @GetMapping
    public List<OrderItem> getAllOrderItems() {
        return orderItemSerOrderItemService.getAllOrderItems();
    }

    @PostMapping
    public OrderItem createOrderItem(@RequestBody OrderItem orderItem) {
        return orderItemSerOrderItemService.createOrderItem(orderItem);
    }

    @GetMapping("/{id}")
    public OrderItem getOrderItemById(@PathVariable Long id) {
        return orderItemSerOrderItemService.getOrderItemById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteOrderItem(@PathVariable Long id) {
        orderItemSerOrderItemService.deleteOrderItem(id);
    }
}
