package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.evento.ApplicationPublisher;
import co.com.pragma.crediya.evento.UpdateApplicationEvent;
import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.exception.ValidationException;
import co.com.pragma.crediya.gateways.UserGateway;
import co.com.pragma.crediya.model.estado.Estado;
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
    private final ApplicationPublisher publisher;

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

    public Mono<PagedResponse<SolicitudInfo>> findApplicationPage(List<String> filtros, int page, int size, String sortDir) {
        String finalSortDir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        return solicitudRepository.countApplication(filtros)
                .flatMap(total -> {
                    int totalPages = (int) Math.ceil((double) total / size);
                    return solicitudRepository.findApplication(filtros, page, size, finalSortDir)
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

    public Mono<Solicitud> updateStatus(Long idSolicitud, Long idEstado) {
        return solicitudRepository.findById(idSolicitud)
                .switchIfEmpty(Mono.error(new ValidationException("Solicitud no encontrada")))
                .flatMap(solicitud -> {
                    solicitud.setIdEstado(idEstado);
                    return solicitudRepository.saveApplication(solicitud);
                })
                // Se consulta el estado
                .zipWhen(solicitudGuardada -> estadoRepository.findById(idEstado))
                // Se consulta el préstamo
                .flatMap(tuple -> {
                    Solicitud solicitudGuardada = tuple.getT1();
                    Estado estado = tuple.getT2();

                    return prestamoRepository.findById(solicitudGuardada.getIdPrestamo())
                            .flatMap(prestamo ->
                                    // Se consulta el usuario por correo
                                    userGateway.getUserByCorreo(solicitudGuardada.getEmail())
                                            .flatMap(usuario -> {
                                                // Mensaje según nombre del estado
                                                String mensaje = switch (estado.getNombre()) {
                                                    case "Aprobado" -> "Ahora eres una persona millonaria :D.";
                                                    case "Rechazado" -> "Lo siento mucho :(.";
                                                    default -> "El estado de su solicitud ha cambiado.";
                                                };

                                                UpdateApplicationEvent event = new UpdateApplicationEvent(
                                                        "SOL-" + solicitudGuardada.getId(),
                                                        estado.getNombre(),
                                                        solicitudGuardada.getEmail(),
                                                        usuario.documentoIdentidad(),
                                                        solicitudGuardada.getMonto().longValue(),
                                                        prestamo.getNombre(),
                                                        mensaje
                                                );

                                                return publisher.send(event)
                                                        .thenReturn(solicitudGuardada);
                                            })
                            );
                });
    }

}
