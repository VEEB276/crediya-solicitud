package co.com.pragma.crediya.api;

import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    @Test
    void shouldReturnBadRequestWhenValidationException() {
        Throwable error = new ValidationException("Datos inválidos");

        Mono<ServerResponse> responseMono = ErrorHandler.handleError(error);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnConflictWhenBusinessException() {
        Throwable error = new BusinessException("Regla de negocio violada");

        Mono<ServerResponse> responseMono = ErrorHandler.handleError(error);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedError() {
        Throwable error = new RuntimeException("Error inesperado");

        Mono<ServerResponse> responseMono = ErrorHandler.handleError(error);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                })
                .verifyComplete();
    }
}
