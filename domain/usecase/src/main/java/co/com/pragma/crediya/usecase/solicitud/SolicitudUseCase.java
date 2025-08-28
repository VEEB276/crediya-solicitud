package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;

    public Mono<Solicitud> saveApplication(Solicitud solicitud) {

        return null;
    }

}
