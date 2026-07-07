package edgareldy.springkafkatutorial.controller;

import edgareldy.springkafkatutorial.dto.common.ApiResponse;
import edgareldy.springkafkatutorial.dto.common.PageResponse;
import edgareldy.springkafkatutorial.dto.order.OrderRequest;
import edgareldy.springkafkatutorial.dto.order.OrderResponse;
import edgareldy.springkafkatutorial.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing CRUD endpoints for orders. Delegates every
 * operation to {@link OrderService}; no business logic lives here, and no
 * Kafka publication happens here either (added on top of create() only in
 * feature/messaging).
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List orders, paginated and optionally filtered by customerId or productId")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    public ApiResponse<PageResponse<OrderResponse>> findAll(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long productId,
            Pageable pageable) {
        return ApiResponse.success(
                orderService.findAll(customerId, productId, pageable), "Orders retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ApiResponse<OrderResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(orderService.findById(id), "Order retrieved successfully");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an order, total is computed automatically")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Product does not have enough stock")
    })
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.create(request), "Order created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an order, total is recomputed automatically")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order, customer or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Product does not have enough stock")
    })
    public ApiResponse<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.update(id, request), "Order updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.success(null, "Order deleted successfully");
    }
}
