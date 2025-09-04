package co.com.pragma.crediya.model.solicitud;

import java.math.BigDecimal;

public record SolicitudInfo(
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
