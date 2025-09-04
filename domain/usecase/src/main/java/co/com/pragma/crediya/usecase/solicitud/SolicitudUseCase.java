package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.gateways.UserGateway;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.prestamo.gateways.PrestamoRepository;
import co.com.pragma.crediya.model.solicitud.PagedResponse;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.SolicitudInfo;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final PrestamoRepository prestamoRepository;
    private final UserGateway userGateway;

    public Mono<Solicitud> saveApplication(Solicitud solicitud) {
        return userGateway.existUserByDocument(solicitud.getDocumentoIdentidad())
                .flatMap(existe -> {
                    if (Boolean.FALSE.equals(existe)) {
                        return Mono.error(new BusinessException("No existe usuario con el documento " + solicitud.getDocumentoIdentidad()));
                    }
                    return prestamoRepository.findById(solicitud.getIdPrestamo())
                            .flatMap(prestamo -> estadoRepository.findBySigla("PEN")
                                    .flatMap(estado -> {
                                        solicitud.setIdEstado(estado.getId());
                                        return solicitudRepository.saveApplication(solicitud);
                                    })
                            )
                            .switchIfEmpty(Mono.error(new BusinessException("El préstamo no existe")));
                });
    }

    public Mono<PagedResponse<SolicitudInfo>> execute(List<String> filtros, int page, int size, String sortDir) {
        String finalSortDir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return solicitudRepository.countPendingSolicitudes(filtros)
                .flatMap(total -> {
                    int totalPages = (int) Math.ceil((double) total / size);
                    return solicitudRepository.findPendingSolicitudes(filtros, page, size, finalSortDir)
                            .collectList()
                            .map(content -> new PagedResponse<>(
                                    content,
                                    page,
                                    size,
                                    total,
                                    totalPages,
                                    "id_solicitud",
                                    finalSortDir
                            ));
                });
    }

}
