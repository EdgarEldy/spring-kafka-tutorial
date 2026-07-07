package edgareldy.springkafkatutorial.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Payload accepted by {@code POST}/{@code PUT} {@code /api/v1/orders}.
 * {@code customerId} and {@code productId} must reference existing
 * resources, and the product must have enough stock for {@code quantity};
 * the service layer, not this record, is responsible for checking both.
 * {@code total} is never part of the request: it is always computed by
 * the service from the product's current unit price and the requested
 * quantity.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public record OrderRequest(

        @NotNull(message = "customerId must not be null")
        Long customerId,

        @NotNull(message = "productId must not be null")
        Long productId,

        @NotNull(message = "quantity must not be null")
        @Positive(message = "quantity must be greater than 0")
        Integer quantity
) {
}
