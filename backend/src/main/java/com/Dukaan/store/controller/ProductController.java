package com.Dukaan.store.controller;

import com.Dukaan.store.dto.ProductDTO;
import com.Dukaan.store.model.Product;
import com.Dukaan.store.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "APIs for managing products in the e-commerce platform")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(
        summary = "Get all products", 
        description = "Retrieve a list of all products available in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of products", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @Operation(
        summary = "Create a new product", 
        description = "Create a new product in the system. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully", 
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Product details to create",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"name\": \"Smartphone\", \"description\": \"Latest Android smartphone with 128GB storage\", \"price\": 299.99, \"stock\": 50}"
                    )
                )
            ) ProductDTO productDTO) {
        Product product = toEntity(productDTO);
        Product saved = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
    }

    @Operation(
        summary = "Get product by ID", 
        description = "Retrieve a specific product by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found successfully", 
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(product));
    }

    @Operation(
        summary = "Update product", 
        description = "Update an existing product. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully", 
                    content = @Content(schema = @Schema(implementation = ProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return ResponseEntity.notFound().build();
        }
        
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        
        Product updated = productService.createProduct(existingProduct);
        return ResponseEntity.ok(toDTO(updated));
    }

    @Operation(
        summary = "Delete product by ID", 
        description = "Remove a product from the system. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Search products", 
        description = "Search products by name, description, or price range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @Parameter(description = "Product name to search", example = "smartphone")
            @RequestParam(required = false) String name,
            @Parameter(description = "Minimum price", example = "100.0")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price", example = "500.0")
            @RequestParam(required = false) Double maxPrice) {
        
        List<Product> products = productService.searchProducts(name, minPrice, maxPrice);
        List<ProductDTO> productDTOs = products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @Operation(
        summary = "Get products with pagination and sorting", 
        description = "Retrieve products with pagination and sorting options"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paginated products retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getProductsPaginated(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productService.getProductsPaginated(pageable);
        
        List<ProductDTO> products = productPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        response.put("hasNext", productPage.hasNext());
        response.put("hasPrevious", productPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update product stock", 
        description = "Update the stock quantity of a specific product. Requires ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Invalid stock quantity"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateProductStock(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody Map<String, Integer> stockUpdate) {
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        
        Integer newStock = stockUpdate.get("stock");
        if (newStock == null || newStock < 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid stock quantity");
            return ResponseEntity.badRequest().body(error);
        }
        
        product.setStock(newStock);
        productService.createProduct(product);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Stock updated successfully");
        response.put("productId", id);
        response.put("newStock", newStock);
        
        return ResponseEntity.ok(response);
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        return product;
    }
}
