package edgareldy.springkafkatutorial.dto.order;

/**
 * Representation of an {@link edgareldy.springkafkatutorial.entity.Order}
 * returned by the API, never the JPA entity itself. Carries the parent
 * customer and product as flat id/name pairs instead of nested
 * {@code CustomerResponse}/{@code ProductResponse} objects, matching the
 * response shape used for {@code ProductResponse.categoryId/categoryName}.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public record OrderResponse(
        Long id,
        Long customerId,
        String customerFirstName,
        String customerLastName,
        Long productId,
        String productName,
        int quantity,
        double total
) {
}
