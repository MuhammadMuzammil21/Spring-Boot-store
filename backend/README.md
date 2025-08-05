# Dukaan E-commerce Backend API

A comprehensive Spring Boot backend application for an e-commerce platform with JWT authentication, comprehensive API documentation, and production-ready features.

## 🚀 Features

### Core Functionality
- **Product Management**: Full CRUD operations with advanced search, filtering, sorting, and pagination
- **Order Processing**: Complete order lifecycle management with status tracking and cancellation
- **User Management**: User registration, authentication, profile management, and role-based access control
- **Authentication & Security**: JWT-based authentication with account lockout protection

### API Documentation
- **Interactive Swagger UI**: Comprehensive API documentation with examples and testing capabilities
- **OpenAPI 3.0 Specification**: Complete API specification with request/response schemas
- **Security Integration**: JWT authentication testing directly in Swagger UI

### Advanced Features
- **Pagination & Sorting**: Efficient data retrieval for large datasets
- **Search & Filtering**: Advanced product search and order filtering capabilities
- **Global Exception Handling**: Consistent error responses with proper HTTP status codes
- **Input Validation**: Comprehensive request validation with detailed error messages
- **CORS Support**: Cross-origin resource sharing for frontend integration

## 🛠 Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL with JPA/Hibernate
- **Documentation**: SpringDoc OpenAPI 3
- **Validation**: Jakarta Bean Validation
- **Build Tool**: Maven
- **Java Version**: 24

## 📋 Prerequisites

- Java 24 or higher
- Maven 3.6+
- PostgreSQL 12+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd dukaan-backend
```

### 2. Database Configuration
Create a PostgreSQL database and update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/dukaan_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# Email Configuration (for password reset)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification
Get the OpenAPI JSON specification at:
```
http://localhost:8080/v3/api-docs
```

## 🔐 Authentication

### JWT Token Authentication
1. **Register** a new user or **login** with existing credentials
2. Use the returned JWT token in the `Authorization` header:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

### Default Admin User
Create an admin user through the registration endpoint with role "ADMIN" or manually in the database.

## 📖 API Endpoints

### Authentication Endpoints
- `POST /login` - User login
- `POST /register` - User registration
- `POST /forgot-password` - Request password reset
- `POST /reset-password` - Reset password with token

### Product Management
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product (Admin only)
- `PUT /api/products/{id}` - Update product (Admin only)
- `DELETE /api/products/{id}` - Delete product (Admin only)
- `GET /api/products/search` - Search products
- `GET /api/products/paginated` - Get paginated products
- `PATCH /api/products/{id}/stock` - Update product stock (Admin only)

### Order Management
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}/status` - Update order status (Admin only)
- `PUT /api/orders/{id}/cancel` - Cancel order
- `DELETE /api/orders/{id}` - Delete order (Admin only)
- `GET /api/orders/filter` - Get filtered orders with pagination
- `GET /api/orders/user/{userId}` - Get user's orders

### User Management
- `GET /api/users` - Get all users (Admin only)
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user (Admin only)
- `PUT /api/users/{id}` - Update user profile
- `DELETE /api/users/{id}` - Delete user (Admin only)
- `GET /api/users/paginated` - Get paginated users (Admin only)
- `PUT /api/users/{id}/password` - Change user password
- `GET /api/users/profile` - Get current user profile

## 🔒 Security Features

### JWT Authentication
- Secure token-based authentication
- Configurable token expiration
- Role-based access control (USER, ADMIN)

### Account Security
- Account lockout after 5 failed login attempts
- 15-minute lockout duration
- Password reset via email
- Secure password encoding with BCrypt

### API Security
- CORS configuration for cross-origin requests
- Method-level security annotations
- Input validation and sanitization
- Comprehensive error handling

## 🧪 Testing

### Using Swagger UI
1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Register a new user or login with existing credentials
3. Copy the JWT token from the login response
4. Click "Authorize" button and enter: `Bearer <your-token>`
5. Test any endpoint directly from the documentation

### Sample API Calls

#### Register a User
```bash
curl -X POST "http://localhost:8080/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

#### Login
```bash
curl -X POST "http://localhost:8080/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePassword123"
  }'
```

#### Create a Product (Admin only)
```bash
curl -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smartphone",
    "description": "Latest Android smartphone with 128GB storage",
    "price": 299.99,
    "stock": 50
  }'
```

## 🚀 Deployment

### Production Configuration
1. Update `application.properties` for production environment
2. Configure proper database connection
3. Set secure JWT secret key
4. Configure email service for password reset
5. Enable HTTPS
6. Set up proper logging configuration

### Docker Deployment
```dockerfile
FROM openjdk:24-jdk-slim
COPY target/dukaan-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact: support@dukaan.com
- Documentation: [API Documentation](http://localhost:8080/swagger-ui.html)

## 🔄 Version History

### v1.0.0 (Current)
- Complete API implementation
- JWT authentication
- Comprehensive documentation
- Advanced search and filtering
- Global exception handling
- Input validation
- CORS support

---

**Built with ❤️ for the Dukaan e-commerce platform**
