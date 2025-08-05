package com.Dukaan.store.service;

public class StripePaymentService implements PaymentService {
    @Override
    public void processPayment(double amount) {
        // Logic to process payment using Stripe
        System.out.println("Stripe");
        System.out.println("Processing payment of amount: " + amount);
    }
}
