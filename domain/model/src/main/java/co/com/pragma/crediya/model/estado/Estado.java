package co.com.pragma.crediya.model.estado;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Estado {

    private Long id;
    private String nombre;
    private String sigla;
    private String descripcion;
}
