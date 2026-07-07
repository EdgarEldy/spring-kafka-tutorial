package edgareldy.springkafkatutorial.dto.category;

/**
 * Representation of a {@link edgareldy.springkafkatutorial.entity.Category}
 * returned by the API, never the JPA entity itself.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public record CategoryResponse(
        Long id,
        String categoryName
) {
}
