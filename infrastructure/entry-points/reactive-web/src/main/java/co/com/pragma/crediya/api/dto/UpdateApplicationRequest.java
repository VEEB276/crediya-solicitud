package co.com.pragma.crediya.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateApplicationRequest(
        @NotBlank(message = "El estado es requerido")
        Long idEstado
) {}
