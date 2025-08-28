package co.com.pragma.crediya.api;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private  final SolicitudUseCase solicitudUseCase;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Solicitud.class)
                .flatMap(solicitudUseCase::saveApplication)
                .flatMap(saveApplication -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(saveApplication)
                ).onErrorResume(ErrorHandler::handleError);
    }
}
