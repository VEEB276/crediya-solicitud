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
import java.util.Collections;
import java.util.List;

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
    public Flux<SolicitudInfo> findApplication(List<String> filtros, int page, int size, String sortDir) {
        log.info("Inicia consulta paginada de solicitudes");

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
        WHERE (:applyFilter = false OR e.sigla IN (:filtros))
        ORDER BY s.id_solicitud %s
        LIMIT :limit OFFSET :offset
    """.formatted(sortDir);

        boolean applyFilter = filtros != null && !filtros.isEmpty();

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("applyFilter", applyFilter)
                .bind("limit", size)
                .bind("offset", page * size);

        if (applyFilter) {
            spec = spec.bind("filtros", filtros);
        } else {
            spec = spec.bind("filtros", Collections.singletonList(""));
        }

        return spec.map((row, metadata) -> new SolicitudInfo(
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
                .flatMapSequential(solicitud -> webClient.getUserByCorreo(solicitud.email())
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
    public Mono<Long> countApplication(List<String> filtros) {
        log.info("Inicia contador de solicitudes");

        String query = """
        SELECT COUNT(*) AS total
        FROM solicitud s
        JOIN estados e ON s.id_estado = e.id_estado
        WHERE (:applyFilter = false OR e.sigla IN (:filtros))
    """;

        boolean applyFilter = filtros != null && !filtros.isEmpty();

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(query)
                .bind("applyFilter", applyFilter);

        if (applyFilter) {
            spec = spec.bind("filtros", filtros);
        } else {
            spec = spec.bind("filtros", Collections.singletonList(""));
        }

        return spec
                .map((row, meta) -> row.get("total", Long.class))
                .one();
    }
}
