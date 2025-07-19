package com.Dukaan.store.service;

import com.Dukaan.store.model.Order;
import com.Dukaan.store.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository OrderRepository;

    public List<Order> getAllOrders() {
        return OrderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return OrderRepository.findById(id).orElse(null);
    }

    public Order createOrder(Order Order) {
        return OrderRepository.save(Order);
    }

    public void deleteOrder(Long id) {
        OrderRepository.deleteById(id);
    }
}
