package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.SolicitudInfo;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import co.com.pragma.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        Long,
        MyReactiveRepository
> implements SolicitudRepository
{
    private static final Logger log = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);

    private final DatabaseClient databaseClient;

    private final UserWebClientAdapter webClient;

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, DatabaseClient databaseClient, UserWebClientAdapter webClient) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
        this.databaseClient = databaseClient;
        this.webClient = webClient;
    }

    @Override
    public Mono<Solicitud> saveApplication(Solicitud solicitud) {
        log.info("Inicia guardado de la solicitud");
        return super.save(solicitud)
                .doOnSuccess(saved -> log.info("Solicitud guardada exitosamente con id: {}", saved.getId()))
                .doOnError(error -> log.error("Error guardando la solicitud: {}", error.getMessage(), error));
    }

    @Override
    public Flux<SolicitudInfo> findPendingSolicitudes(String filtro, int page, int size) {
        String sql = """
        SELECT s.monto, s.plazo, s.email,
               p.nombre AS tipo_prestamo, p.tasa_interes,
               e.nombre AS estado_solicitud,
               (
                  SELECT COALESCE(SUM(sa.monto),0)
                  FROM solicitud sa
                  WHERE sa.email = s.email
                    AND sa.id_estado = (SELECT id_estado FROM estados WHERE sigla = 'APR')
               ) AS deuda_total_mensual_solicitudes_aprobadas
        FROM solicitud s
        JOIN tipo_prestamo p ON s.id_tipo_prestamo = p.id_tipo_prestamo
        JOIN estados e ON s.id_estado = e.id_estado
        WHERE (:filtro IS NULL OR LOWER(e.sigla) LIKE LOWER(:filtro))
        LIMIT :limit OFFSET :offset
    """;

        return databaseClient.sql(sql)
                .bind("limit", size)
                .bind("offset", page * size)
                .bind("filtro", filtro == null ? null : "%" + filtro + "%")
                .map((row, metadata) -> new SolicitudInfo(
                        row.get("monto", BigDecimal.class),
                        row.get("plazo", Integer.class),
                        row.get("email", String.class),
                        null,
                        row.get("tipo_prestamo", String.class),
                        row.get("tasa_interes", BigDecimal.class),
                        row.get("estado_solicitud", String.class),
                        null,
                        row.get("deuda_total_mensual_solicitudes_aprobadas", BigDecimal.class)
                ))
                .all()
                .flatMap(solicitud -> webClient.getUserByCorreo(solicitud.email())
                        .map(usuario -> new SolicitudInfo(
                                solicitud.monto(),
                                solicitud.plazo(),
                                solicitud.email(),
                                usuario.nombre(),
                                solicitud.tipoPrestamo(),
                                solicitud.tasaInteres(),
                                solicitud.estadoSolicitud(),
                                usuario.salarioBase(),
                                solicitud.deudaTotalMensualSolicitudesAprobadas()
                        ))
                );
    }

    @Override
    public Mono<Long> countPendingSolicitudes(String filtro) {
        String query = """
        SELECT COUNT(*) AS total
        FROM solicitud s
        JOIN estados e ON s.id_estado = e.id_estado
        WHERE (:filtro IS NULL OR LOWER(e.sigla) LIKE LOWER(:filtro))
    """;

        return databaseClient.sql(query)
                .bind("filtro", filtro == null ? null : "%" + filtro + "%")
                .map((row, meta) -> row.get("total", Long.class))
                .one();
    }
}
