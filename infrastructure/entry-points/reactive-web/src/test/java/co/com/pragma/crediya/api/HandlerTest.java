package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.CreateApplicationDTO;
import co.com.pragma.crediya.api.dto.ResponseApplicationDTO;
import co.com.pragma.crediya.api.mapper.ApplicationDtoMapper;
import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.usecase.solicitud.SolicitudUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

class HandlerTest {

    @Mock
    private SolicitudUseCase solicitudUseCase;

    @Mock
    private ApplicationDtoMapper mapper;

    @Mock
    private Validator validator;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Handler handler = new Handler(solicitudUseCase, mapper, validator);

        webTestClient = WebTestClient.bindToRouterFunction(
                route(POST("/solicitud"), handler::listenSaveApplication)
        ).build();
    }

    @Test
    void listenSaveApplicationExitoso() {
        CreateApplicationDTO dto = new CreateApplicationDTO(BigDecimal.ONE, "1234", "val@gmail.com",
                5, 1L, 1L);
        Solicitud solicitud = new Solicitud();
        Solicitud saved = new Solicitud();
        ResponseApplicationDTO response = new ResponseApplicationDTO(1L, BigDecimal.ONE, "1234", "val@gmail.com",
                5, 1L, 1L);

        when(validator.validate(dto)).thenReturn(Collections.emptySet());
        when(mapper.toModel(dto)).thenReturn(solicitud);
        when(solicitudUseCase.saveApplication(solicitud)).thenReturn(Mono.just(saved));
        when(mapper.toResponse(saved)).thenReturn(response);

        webTestClient.post()
                .uri("/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseApplicationDTO.class)
                .isEqualTo(response);

        verify(validator).validate(dto);
        verify(mapper).toModel(dto);
        verify(solicitudUseCase).saveApplication(solicitud);
        verify(mapper).toResponse(saved);
    }

    @Test
    void listenSaveApplicationValidacionInvalida() {
        CreateApplicationDTO dto = new CreateApplicationDTO(BigDecimal.ONE, "1234", "val@gmail.com",
                5, 1L, 1L);

        ConstraintViolation<CreateApplicationDTO> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Campo inválido");

        Set<ConstraintViolation<CreateApplicationDTO>> violations = Set.of(violation);

        when(validator.validate(dto)).thenReturn(violations);

        webTestClient.post()
                .uri("/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest();

        verify(validator).validate(dto);
        verifyNoInteractions(mapper, solicitudUseCase);
    }

    @Test
    void listenSaveApplicationErrorUseCase() {
        CreateApplicationDTO dto = new CreateApplicationDTO(BigDecimal.ONE, "1234", "val@gmail.com",
                5, 1L, 1L);
        Solicitud solicitud = new Solicitud();

        when(validator.validate(dto)).thenReturn(Collections.emptySet());
        when(mapper.toModel(dto)).thenReturn(solicitud);
        when(solicitudUseCase.saveApplication(solicitud))
                .thenReturn(Mono.error(new BusinessException("Falla de negocio")));

        webTestClient.post()
                .uri("/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().is4xxClientError();

        verify(validator).validate(dto);
        verify(mapper).toModel(dto);
        verify(solicitudUseCase).saveApplication(solicitud);
    }

}
