package co.com.pragma.crediya.api;

import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    public static Mono<ServerResponse> handleError(Throwable error) {
        log.error("Error capturado: ", error);

        if (error instanceof ValidationException) {
            return ServerResponse.badRequest()
                    .bodyValue(new ErrorResponse(400, error.getMessage(), null, LocalDateTime.now()));
        }
        if (error instanceof BusinessException) {
            return ServerResponse.status(HttpStatus.CONFLICT)
                    .bodyValue(new ErrorResponse(409, error.getMessage(), null, LocalDateTime.now()));
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue(new ErrorResponse(500, "Error inesperado", null, LocalDateTime.now()));
    }

    private ErrorHandler() { }
}
