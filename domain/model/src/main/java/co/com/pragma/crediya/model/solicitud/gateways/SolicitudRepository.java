package co.com.pragma.crediya.model.solicitud.gateways;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.SolicitudInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SolicitudRepository {

    Mono<Solicitud> saveApplication(Solicitud solicitud);

    Flux<SolicitudInfo> findPendingSolicitudes(List<String> filtro, int page, int size, String sortDir);

    Mono<Long> countPendingSolicitudes(List<String> filtro);

}
