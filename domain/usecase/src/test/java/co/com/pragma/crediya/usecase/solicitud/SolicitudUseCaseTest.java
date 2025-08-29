package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.model.estado.Estado;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.prestamo.Prestamo;
import co.com.pragma.crediya.model.prestamo.gateways.PrestamoRepository;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SolicitudUseCaseTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    private Solicitud solicitud;
    private Estado estado;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        solicitud = new Solicitud();
        solicitud.setIdPrestamo(1L);

        estado = new Estado();
        estado.setId(10L);
        estado.setSigla("PEN");
    }

    @Test
    void saveApplicationSuccess() {
        when(prestamoRepository.findById(1L)).thenReturn(Mono.just(new Prestamo()));
        when(estadoRepository.findBySigla("PEN")).thenReturn(Mono.just(estado));
        when(solicitudRepository.saveApplication(any(Solicitud.class))).thenAnswer(invocation -> {
            Solicitud s = invocation.getArgument(0);
            return Mono.just(s);
        });

        Mono<Solicitud> result = solicitudUseCase.saveApplication(solicitud);

        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertEquals(10L, saved.getIdEstado());
                })
                .verifyComplete();
    }

    @Test
    void saveApplicationPrestamoNotFound() {
        when(prestamoRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<Solicitud> result = solicitudUseCase.saveApplication(solicitud);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                throwable.getMessage().equals("El préstamo no existe")
                )
                .verify();
    }
}
