package edgareldy.springkafkatutorial.dto.customer;

/**
 * Representation of a {@link edgareldy.springkafkatutorial.entity.Customer}
 * returned by the API, never the JPA entity itself.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String telephone,
        String email,
        String address
) {
}
