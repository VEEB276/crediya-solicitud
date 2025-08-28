package co.com.pragma.crediya.r2dbc.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "estados")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EstadoEntity {

    @Id
    @Column("id_estado")
    private Long id;

    private String nombre;

    private String sigla;

    private String descripcion;

}
