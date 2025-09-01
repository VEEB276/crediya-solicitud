package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.CreateApplicationDTO;
import co.com.pragma.crediya.api.mapper.ApplicationDtoMapper;
import co.com.pragma.crediya.exception.ValidationException;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Handler {

    private  final SolicitudUseCase solicitudUseCase;

    private final ApplicationDtoMapper mapper;

    private final Validator validator;

    private static final Logger log = LoggerFactory.getLogger(Handler.class);

    public Mono<ServerResponse> listenSaveApplication(ServerRequest serverRequest) {
        log.info("Inicio de la petición para guardar la solicitud");

        return serverRequest.bodyToMono(CreateApplicationDTO.class)
                .doOnNext(solicitud -> log.info("Solicitud recibida: {}", solicitud))
                .flatMap(dto -> {
                    Set<ConstraintViolation<CreateApplicationDTO>> violations =  validator.validate(dto);
                    if (!violations.isEmpty()) {
                        return Mono.error(new ValidationException(violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "))));
                    }
                    return Mono.just(dto);
                })
                .map(mapper::toModel)
                .flatMap(solicitudUseCase::saveApplication)
                .doOnNext(saveApplication -> log.info("Solicitud guardada con éxito: {}", saveApplication))
                .flatMap(saveApplication -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(mapper.toResponse(saveApplication))
                ).doOnError(e -> log.error("Error al guardar la solicitud", e))
                .onErrorResume(ErrorHandler::handleError)
                .doFinally(signal -> log.info("Fin de la petición para guardar la solicitud"));
    }
}
