package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import co.com.pragma.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        Long,
        MyReactiveRepository
> implements SolicitudRepository
{
    private static final Logger log = LoggerFactory.getLogger(MyReactiveRepositoryAdapter.class);

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
    }

    @Override
    public Mono<Solicitud> saveApplication(Solicitud solicitud) {
        log.info("Inicia guardado de la solicitud");
        return super.save(solicitud)
                .doOnSuccess(saved -> log.info("Solicitud guardada exitosamente con id: {}", saved.getId()))
                .doOnError(error -> log.error("Error guardando la solicitud: {}", error.getMessage(), error));
    }
}
