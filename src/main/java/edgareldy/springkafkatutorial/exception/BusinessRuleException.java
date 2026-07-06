package edgareldy.springkafkatutorial.exception;

/**
 * Thrown by service implementations when an operation violates a domain
 * rule (e.g. creating an Order for a Product without enough stock).
 * Caught by {@link GlobalExceptionHandler} and translated into a 422 response.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-kafka-tutorial
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
