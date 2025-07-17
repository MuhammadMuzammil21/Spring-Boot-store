package com.Dukaan.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private PaymentService paymentService;

    public OrderService() {
       // Default constructor
    }

    @Autowired
    public OrderService(PaymentService paymentService) {
        // Constructor logic if needed
        this.paymentService = paymentService;
    }

    public void placeOrder() {
        paymentService.processPayment(100.0);
    }

    public void setPaymentService(PaymentService paymentService) {
        // Logic to set the payment service if needed
        this.paymentService = paymentService;
    }
}
