# 🏪 Dukaan Store — Spring Boot Application

A robust store backend built using **Spring Boot**, demonstrating key Spring concepts like Dependency Injection, Controllers, REST APIs, JWT-based authentication, and payment service simulation.

---

## 📂 Project Structure

```
backend/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/Dukaan/store/
│   │   │   ├── StoreApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── OrderItemController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/
│   │   │   │   ├── OrderDTO.java
│   │   │   │   ├── OrderItemDTO.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   └── UserDTO.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Product.java
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── OrderItemRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   ├── service/
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── OrderItemService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── PaypalPaymentService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── StripePaymentService.java
│   │   │   │   └── UserService.java
│   │   └── resources/
│   │       ├── static/
│   │       │   └── index.html
│   │       └── application.properties
│   └── test/
│       └── java/com/Dukaan/store/StoreApplicationTests.java
└── target/
```

---

## 🛠️ Tech Stack

- Java 17+ (pom.xml specifies Java 24, but 17+ is recommended for Spring Boot 3.x)
- Spring Boot 3.5.3
- Spring Data JPA
- Spring Security (JWT-based)
- PostgreSQL
- Maven
- Springdoc OpenAPI (Swagger UI)
- JUnit 5

---

## 🚀 Getting Started

### Requirements
- Java 17+
- Maven
- PostgreSQL (running on localhost:5432, database: `dukaan_db`)
- IDE (VS Code or IntelliJ)
- Postman or any REST client

### Setup
1. Clone the repository.
2. Configure your PostgreSQL credentials in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=YOUR_DB_USER
   spring.datasource.password=YOUR_DB_PASSWORD
   ```
3. (Optional) Configure mail settings for password reset.
4. Build and run:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
5. Visit: [http://localhost:8080](http://localhost:8080)
6. API docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🔐 Authentication & Security
- JWT-based authentication for all endpoints except `/login` and `/register`.
- Role-based access: `/api/products` and `/api/products/*` require `ADMIN` role.
- Account lockout after 5 failed login attempts (15 min lock).
- Passwords are hashed with BCrypt.

### Auth Endpoints
- `POST /login` — Login with email & password, returns JWT.
- `POST /register` — Register a new user.
- `POST /forgot-password` — Request password reset link (email required).
- `POST /reset-password` — Reset password using token.

---

## 📘 Key Files & Features
- **Controllers:** Auth, User, Product, Order, OrderItem
- **DTOs:** UserDTO, ProductDTO, OrderDTO, OrderItemDTO
- **Services:** UserService, ProductService, OrderService, OrderItemService, EmailService, CustomUserDetailsService
- **Payment:** `PaymentService` (interface), `StripePaymentService`, `PaypalPaymentService` (strategy pattern)
- **Security:** JWT filter, SecurityConfig, BCrypt password encoding
- **Exception Handling:** GlobalExceptionHandler
- **Static Content:** `index.html` served at root (`/`)
- **OpenAPI/Swagger:** API docs at `/swagger-ui.html`

---

## 🧠 Concepts Demonstrated
- Spring Boot initialization & auto-configuration
- IoC (Inversion of Control) and Bean management
- Constructor-based dependency injection
- Interface-oriented design
- JWT authentication & role-based authorization
- Serving static HTML from `/resources/static/`
- Global exception handling
- DTO pattern for API responses

---

## 🧪 Testing
- JUnit 5 test: `StoreApplicationTests.java` (basic context load)

---

## 📄 License
This project is for educational/demo purposes.



