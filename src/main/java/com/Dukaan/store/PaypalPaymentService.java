package com.Dukaan.store;

import org.springframework.stereotype.Service;

@Service
public class PaypalPaymentService implements PaymentService {
    @Override
    public void processPayment(double amount) {
        // Logic to process payment using PayPal
        System.out.println("PayPal");
        System.out.println("Processing payment of amount: " + amount);
    }
    
}
