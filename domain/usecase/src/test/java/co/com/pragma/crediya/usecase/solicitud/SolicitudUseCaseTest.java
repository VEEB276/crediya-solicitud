package co.com.pragma.crediya.usecase.solicitud;

import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.gateways.UserGateway;
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

import static org.mockito.Mockito.*;

class SolicitudUseCaseTest {

    @Mock
    private UserGateway userGateway;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private SolicitudUseCase solicitudUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveApplicationUsuarioNoExiste() {
        Solicitud solicitud = new Solicitud();
        solicitud.setDocumentoIdentidad("123456");

        when(userGateway.existUserByDocument("123456")).thenReturn(Mono.just(false));

        StepVerifier.create(solicitudUseCase.saveApplication(solicitud))
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        error.getMessage().equals("No existe usuario con el documento 123456"))
                .verify();

        verify(userGateway).existUserByDocument("123456");
        verifyNoInteractions(prestamoRepository, estadoRepository, solicitudRepository);
    }

    @Test
    void saveApplicationPrestamoNoExiste() {
        Solicitud solicitud = new Solicitud();
        solicitud.setDocumentoIdentidad("123456");
        solicitud.setIdPrestamo(1L);

        when(userGateway.existUserByDocument("123456")).thenReturn(Mono.just(true));
        when(prestamoRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.saveApplication(solicitud))
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        error.getMessage().equals("El préstamo no existe"))
                .verify();

        verify(userGateway).existUserByDocument("123456");
        verify(prestamoRepository).findById(1L);
        verifyNoInteractions(estadoRepository, solicitudRepository);
    }

    @Test
    void saveApplicationExitoso() {
        Solicitud solicitud = new Solicitud();
        solicitud.setDocumentoIdentidad("123456");
        solicitud.setIdPrestamo(1L);

        Prestamo prestamo = new Prestamo();
        Estado estado = new Estado();
        estado.setId(99L);

        Solicitud solicitudGuardada = new Solicitud();
        solicitudGuardada.setDocumentoIdentidad("123456");
        solicitudGuardada.setIdPrestamo(1L);
        solicitudGuardada.setIdEstado(99L);

        when(userGateway.existUserByDocument("123456")).thenReturn(Mono.just(true));
        when(prestamoRepository.findById(1L)).thenReturn(Mono.just(prestamo));
        when(estadoRepository.findBySigla("PEN")).thenReturn(Mono.just(estado));
        when(solicitudRepository.saveApplication(solicitud)).thenReturn(Mono.just(solicitudGuardada));

        StepVerifier.create(solicitudUseCase.saveApplication(solicitud))
                .expectNextMatches(result ->
                        result.getDocumentoIdentidad().equals("123456")
                                && result.getIdPrestamo().equals(1L)
                                && result.getIdEstado().equals(99L))
                .verifyComplete();

        verify(userGateway).existUserByDocument("123456");
        verify(prestamoRepository).findById(1L);
        verify(estadoRepository).findBySigla("PEN");
        verify(solicitudRepository).saveApplication(solicitud);
    }

    @Test
    void saveApplicationErrorEnRepositorio() {
        Solicitud solicitud = new Solicitud();
        solicitud.setDocumentoIdentidad("123456");

        when(userGateway.existUserByDocument("123456"))
                .thenReturn(Mono.error(new RuntimeException("Falla en BD")));

        StepVerifier.create(solicitudUseCase.saveApplication(solicitud))
                .expectErrorMatches(error -> error instanceof RuntimeException &&
                        error.getMessage().equals("Falla en BD"))
                .verify();

        verify(userGateway).existUserByDocument("123456");
        verifyNoInteractions(prestamoRepository, estadoRepository, solicitudRepository);
    }
}
