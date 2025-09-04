package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.SolicitudInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {

    Mono<Solicitud> saveApplication(Solicitud solicitud);

    Flux<SolicitudInfo> findPendingSolicitudes(String filtro, int page, int size);

    Mono<Long> countPendingSolicitudes(String filtro);

}
