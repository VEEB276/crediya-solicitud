package co.com.pragma.crediya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateApplicationDTO (
        @NotNull(message = "El monto es requerido")
        BigDecimal monto,

        @NotBlank(message = "El documento es requerido")
        String documentoIdentidad,

        @Email(message = "El email requiere formato correcto")
        String email,

        Integer plazo,

        Long idEstado,

        @NotNull(message = "El prestamos es requerido")
        Long idPrestamo
) {}
