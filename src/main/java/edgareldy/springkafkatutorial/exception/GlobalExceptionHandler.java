package edgareldy.springkafkatutorial.exception;

import edgareldy.springkafkatutorial.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler that translates every exception thrown by a
 * controller (directly or through a service) into a consistent
 * {@code ApiResponse<ErrorResponse>}, so callers never have to parse a
 * different error shape depending on which endpoint failed.
 * <p>
 * Created edgar.muhamyangabo on 7/6/26
 * Author : edgar.muhamyangabo
 * Date : 7/6/26
 * Project : spring-kafka-tutorial
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, null);
    }

    private ErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ApiResponse<ErrorResponse>> buildResponse(
            HttpStatus status, String message, HttpServletRequest request,
            List<ErrorResponse.FieldError> fieldErrors) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
        ApiResponse<ErrorResponse> body = new ApiResponse<>(false, message, errorResponse, errorResponse.timestamp());
        return ResponseEntity.status(status).body(body);
    }
}
