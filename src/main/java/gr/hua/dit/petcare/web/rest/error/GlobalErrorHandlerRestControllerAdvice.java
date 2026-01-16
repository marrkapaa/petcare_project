package gr.hua.dit.petcare.web.rest.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Πιο κοινό Exception
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice(basePackages = "gr.hua.dit.petcare.web.rest") // ΠΡΟΣΟΧΗ ΣΤΟ ΠΑΚΕΤΟ
@Order(1)
public class GlobalErrorHandlerRestControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandlerRestControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAnyError(final Exception exception,
                                                   final HttpServletRequest httpServletRequest) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        // Έλεγχος τύπου Exception
        if (exception instanceof NoResourceFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (exception instanceof SecurityException || exception instanceof AccessDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN; // Ή UNAUTHORIZED ανάλογα την περίπτωση
        } else if (exception instanceof IllegalArgumentException) {
            httpStatus = HttpStatus.BAD_REQUEST; // Για Validation Errors
        } else if (exception instanceof ResponseStatusException responseStatusException) {
            httpStatus = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }

        // Logging
        LOGGER.warn("REST error [{} {}] -> status={} cause={}: {}",
            httpServletRequest.getMethod(),
            httpServletRequest.getRequestURI(),
            httpStatus.value(),
            exception.getClass().getSimpleName(),
            exception.getMessage()
        );

        // Δημιουργία απάντησης
        final ApiError apiError = new ApiError(
            Instant.now(),
            httpStatus.value(),
            httpStatus.getReasonPhrase(),
            exception.getMessage(),
            httpServletRequest.getRequestURI()
        );

        return ResponseEntity.status(httpStatus).body(apiError);
    }
}
