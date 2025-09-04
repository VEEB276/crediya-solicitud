package co.com.pragma.crediya.api.dto;

import java.math.BigDecimal;

public record SolicitudResponseDTO(
        BigDecimal monto,
        Integer plazo,
        String email,
        String nombre,
        String tipoPrestamo,
        BigDecimal tasaInteres,
        String estadoSolicitud,
        BigDecimal salarioBase,
        BigDecimal deudaTotalMensualSolicitudesAprobadas
) {}
