# 🏪 Dukaan Store — Spring Boot Application

### A basic store backend built using **Spring Boot**, demonstrating key Spring concepts like Dependency Injection, Controllers, and REST APIs. This project simulates a sample payment service.
---

## 📂 Project Structure

```

dukaan-store/
├── src/
│   ├── main/
│   │   ├── java/com/Dukaan/store/
│   │   │   ├── HomeController.java
│   │   │   ├── OrderService.java
│   │   │   ├── PaymentService.java
│   │   │   ├── PaypalPaymentService.java
│   │   │   ├── StripePaymentService.java
│   │   │   └── StoreApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html
│   │       └── application.properties
├── pom.xml

````

### Requirements

- Java 17+
- Maven
- IDE (VS Code or IntelliJ)
- Postman or any REST client

### Run Locally

```bash
./mvnw spring-boot:run
````

Visit: `http://localhost:8080`

## 📘 Key Files

* **`PaymentService.java`**: Interface defining the `processPayment()` method.
* **`StripePaymentService.java` / `PaypalPaymentService.java`**: Implements payment processing logic.
* **`OrderService.java`**: Injects the payment service using constructor injection.
* **`StoreApplication.java`**: Main Spring Boot application entry point.

---

## 🧠 Concepts Demonstrated

* Spring Boot initialization & auto-configuration
* IoC (Inversion of Control) and Bean management
* Constructor-based dependency injection
* Interface-oriented design
* Serving static HTML from `/resources/static/`

---

## 🛠️ Tech Stack

* Java
* Spring Boot
* Maven



