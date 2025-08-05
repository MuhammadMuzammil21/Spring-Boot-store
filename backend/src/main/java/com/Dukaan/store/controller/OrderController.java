package com.Dukaan.store.controller;

import com.Dukaan.store.dto.OrderDTO;
import com.Dukaan.store.dto.UserDTO;
import com.Dukaan.store.dto.OrderItemDTO;
import com.Dukaan.store.model.Order;
import com.Dukaan.store.model.User;
import com.Dukaan.store.model.OrderItem;
import com.Dukaan.store.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for managing orders and order processing")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(
        summary = "Get all orders", 
        description = "Retrieve a list of all orders in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @Operation(
        summary = "Create a new order (checkout)", 
        description = "Create a new order with specified items and user information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully", 
                    content = @Content(schema = @Schema(implementation = OrderDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody OrderDTO orderDTO) {
        Order order = toEntity(orderDTO);
        Order saved = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @Operation(
        summary = "Get order by ID", 
        description = "Retrieve a specific order by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found successfully", 
                    content = @Content(schema = @Schema(implementation = OrderDTO.class))),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(order));
    }

    @Operation(
        summary = "Update order status", 
        description = "Update the status of an existing order (e.g., pending, processing, shipped, delivered, cancelled)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        String newStatus = statusUpdate.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Status is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Validate status values
        List<String> validStatuses = List.of("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(newStatus.toUpperCase())) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid status. Valid statuses: " + validStatuses);
            return ResponseEntity.badRequest().body(error);
        }
        
        orderService.updateOrderStatus(id, newStatus.toUpperCase());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order status updated successfully");
        response.put("orderId", id);
        response.put("newStatus", newStatus.toUpperCase());
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get orders with filtering and pagination", 
        description = "Retrieve orders with optional filtering by status, date range, and pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filtered orders retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getOrdersFiltered(
            @Parameter(description = "Order status filter", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-12-31")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        LocalDate start = null;
        LocalDate end = null;
        
        try {
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDate.parse(endDate);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid date format. Use YYYY-MM-DD");
            return ResponseEntity.badRequest().body(error);
        }
        
        Page<Order> orderPage = orderService.getOrdersFiltered(status, start, end, pageable);
        
        List<OrderDTO> orders = orderPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders);
        response.put("currentPage", orderPage.getNumber());
        response.put("totalItems", orderPage.getTotalElements());
        response.put("totalPages", orderPage.getTotalPages());
        response.put("hasNext", orderPage.hasNext());
        response.put("hasPrevious", orderPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get user's orders", 
        description = "Retrieve all orders for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User orders retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getUserOrders(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        
        List<Order> userOrders = orderService.getOrdersByUserId(userId);
        List<OrderDTO> orderDTOs = userOrders.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(orderDTOs);
    }

    @Operation(
        summary = "Cancel order", 
        description = "Cancel an existing order if it's in a cancellable state"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long id) {
        
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            orderService.cancelOrder(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order cancelled successfully");
            response.put("orderId", id);
            response.put("status", "CANCELLED");
            
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(
        summary = "Delete order by ID", 
        description = "Remove an order from the system. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setUser(toUserDTO(order.getUser()));
        dto.setItems(order.getItems().stream().map(this::toOrderItemDTO).collect(Collectors.toList()));
        dto.setTotal(order.getTotalPrice());
        return dto;
    }

    private Order toEntity(OrderDTO dto) {
        Order order = new Order();
        // User and items should be set in service layer for real use
        order.setTotalPrice(dto.getTotal());
        return order;
    }

    private UserDTO toUserDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getProduct() != null ? item.getProduct().getPrice() : 0);
        // Product mapping omitted for brevity
        return dto;
    }
}
