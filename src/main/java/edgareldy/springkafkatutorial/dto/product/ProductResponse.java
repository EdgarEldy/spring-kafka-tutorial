package edgareldy.springkafkatutorial.dto.product;

/**
 * Representation of a {@link edgareldy.springkafkatutorial.entity.Product}
 * returned by the API. Carries the parent category as a flat
 * {@code categoryId}/{@code categoryName} pair instead of a nested
 * {@code CategoryResponse}, matching the response shape documented in the
 * README.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public record ProductResponse(
        Long id,
        String productName,
        float unitPrice,
        int stockQuantity,
        Long categoryId,
        String categoryName
) {
}
