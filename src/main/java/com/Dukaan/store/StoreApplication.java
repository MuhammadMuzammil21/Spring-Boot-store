package com.Dukaan.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StoreApplication {

	public static void main(String[] args) {
		
		org.springframework.context.ApplicationContext context = SpringApplication.run(StoreApplication.class, args);
		context.getBean(OrderService.class);
		var orderService = context.getBean(OrderService.class);
		orderService.placeOrder();
	}

}
