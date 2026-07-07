package edgareldy.springkafkatutorial.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;

/**
 * Payload accepted by {@code POST}/{@code PUT} {@code /api/v1/products}.
 * {@code categoryId} must reference an existing category; the service
 * layer, not this record, is responsible for checking that.
 * {@code stockQuantity} accepts zero (out of stock) but never a negative
 * value, matching the {@code products.stock_quantity} CHECK constraint.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public record ProductRequest(

        @NotNull(message = "categoryId must not be null")
        Long categoryId,

        @NotBlank(message = "productName must not be blank")
        String productName,

        @NotNull(message = "unitPrice must not be null")
        @Positive(message = "unitPrice must be greater than 0")
        Float unitPrice,

        @NotNull(message = "stockQuantity must not be null")
        @PositiveOrZero(message = "stockQuantity must not be negative")
        Integer stockQuantity
) {
}
