package com.Dukaan.store.controller;

import com.Dukaan.store.dto.OrderDTO;
import com.Dukaan.store.dto.UserDTO;
import com.Dukaan.store.dto.OrderItemDTO;
import com.Dukaan.store.model.Order;
import com.Dukaan.store.model.User;
import com.Dukaan.store.model.OrderItem;
import com.Dukaan.store.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Get all orders", responses = {
            @ApiResponse(responseCode = "200", description = "List of orders", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDTO.class)))
    })
    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Create a new order (checkout)", requestBody = @RequestBody(content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"user\":{\"email\":\"test@example.com\"},\"items\":[{\"product\":{\"name\":\"Widget\"},\"quantity\":2}]}"))), responses = {
            @ApiResponse(responseCode = "200", description = "Order created", content = @Content(schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock")
    })
    @PostMapping
    public OrderDTO createOrder(@RequestBody OrderDTO orderDTO) {
        Order order = toEntity(orderDTO);
        Order saved = orderService.createOrder(order);
        return toDTO(saved);
    }

    @Operation(summary = "Get order by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderDTO.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public OrderDTO getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return order != null ? toDTO(order) : null;
    }

    @Operation(summary = "Delete order by ID", responses = {
            @ApiResponse(responseCode = "204", description = "Order deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
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
