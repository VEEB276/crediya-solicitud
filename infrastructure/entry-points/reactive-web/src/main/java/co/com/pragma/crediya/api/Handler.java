package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.CreateApplicationDTO;
import co.com.pragma.crediya.api.dto.UpdateApplicationRequest;
import co.com.pragma.crediya.api.mapper.ApplicationDtoMapper;
import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.exception.ValidationException;
import co.com.pragma.crediya.gateways.UserGateway;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Handler {

    private  final SolicitudUseCase solicitudUseCase;

    private final ApplicationDtoMapper mapper;

    private final Validator validator;

    private final UserGateway userGateway;

    private static final Logger log = LoggerFactory.getLogger(Handler.class);

    public Mono<ServerResponse> listenSaveApplication(ServerRequest serverRequest) {
        log.info("Inicio de la petición para guardar la solicitud");

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    String email = auth.getName();

                    return serverRequest.bodyToMono(CreateApplicationDTO.class)
                            .doOnNext(solicitud -> log.info("Solicitud recibida: {}", solicitud))
                            .flatMap(dto -> {
                                Set<ConstraintViolation<CreateApplicationDTO>> violations = validator.validate(dto);
                                if (!violations.isEmpty()) {
                                    return Mono.error(new ValidationException(
                                            violations.stream()
                                                    .map(ConstraintViolation::getMessage)
                                                    .collect(Collectors.joining(", "))));
                                }
                                return Mono.just(dto);
                            })
                            .map(mapper::toModel)
                            .flatMap(solicitud -> {
                                if (!email.equals(solicitud.getEmail())) {
                                    return Mono.error(new BusinessException(
                                            "No puedes guardar una solicitud de otro usuario"));
                                }
                                return solicitudUseCase.saveApplication(solicitud);
                            })
                            .doOnNext(saveApplication -> log.info("Solicitud guardada con éxito: {}", saveApplication))
                            .flatMap(saveApplication -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(mapper.toResponse(saveApplication))
                            )
                            .doOnError(e -> log.error("Error al guardar la solicitud", e))
                            .onErrorResume(ErrorHandler::handleError)
                            .doFinally(signal -> log.info("Fin de la petición para guardar la solicitud"));
                });
    }

    public Mono<ServerResponse> listarSolicitudes(ServerRequest request) {

        String filtrosParam = request.queryParam("filtros").orElse("");

        List<String> filtros = Arrays.stream(
                        filtrosParam.replaceAll("[\\[\\]\\s]", "")
                                .split(",")
                )
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .toList();

        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortDir = request.queryParam("sortDir").orElse("DESC");

        return solicitudUseCase.findApplicationPage(filtros, page, size, sortDir)
                .flatMap(pagedResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pagedResponse));
    }

    public Mono<ServerResponse> updateSolicitud(ServerRequest request) {
        Long idSolicitud = Long.valueOf(request.pathVariable("idSolicitud"));
        log.info("Inicio petición para actualizar solicitud con id: {}", idSolicitud);

        Mono<UpdateApplicationRequest> bodyMono = request.bodyToMono(UpdateApplicationRequest.class)
                .doOnNext(req -> log.debug("Body recibido: {}", req));

        return bodyMono.flatMap(req -> {
            log.info("Invocación de solicitudUseCase");

            return solicitudUseCase.updateStatus(idSolicitud, req.idEstado())
                    .doOnSuccess(solicitud ->
                            log.info("Solicitud actualizada exitosamente: {}", solicitud))
                    .doOnError(error ->
                            log.error("Error actualizando solicitud con id={}", idSolicitud, error))
                    .flatMap(solicitudActualizada ->
                            ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(solicitudActualizada)
                    );
        });
    }

}
