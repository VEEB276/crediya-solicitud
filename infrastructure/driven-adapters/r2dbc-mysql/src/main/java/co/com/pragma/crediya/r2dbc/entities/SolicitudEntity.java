package co.com.pragma.crediya.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "solicitud")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SolicitudEntity {

    @Id
    @Column("id_solicitud")
    private Long idSolicitud;

    private BigDecimal monto;

    @Column("documento_identidad")
    private String documentoIdentidad;

    private String email;

    private Integer plazo;

    @Column("id_estado")
    private Long idEstado;

    @Column("id_tipo_prestamo")
    private Long idPrestamo;

}
