package co.com.pragma.crediya.api.dto;

import java.math.BigDecimal;

public record ResponseApplicationDTO(
        Long id,
        BigDecimal monto,
        String documentoIdentidad,
        String email,
        Integer plazo,
        Long idEstado,
        Long idPrestamo
) {}
