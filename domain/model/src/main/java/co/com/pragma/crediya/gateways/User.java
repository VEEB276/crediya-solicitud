package co.com.pragma.crediya.gateways;

import java.math.BigDecimal;

public record User(
        Long idUsuario,
        String nombre,
        String apellido,
        String fechaNacimiento,
        String direccion,
        String telefono,
        String correoElectronico,
        BigDecimal salarioBase,
        String documentoIdentidad,
        String password,
        Long idRol
) {}
