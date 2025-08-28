package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.prestamo.Prestamo;
import co.com.pragma.crediya.model.prestamo.gateways.PrestamoRepository;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.crediya.r2dbc.entities.PrestamoEntity;
import co.com.pragma.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PrestamoReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Prestamo,
        PrestamoEntity,
        Long,
        PrestamoReactiveRepository
> implements PrestamoRepository
{
    private static final Logger log = LoggerFactory.getLogger(PrestamoReactiveRepositoryAdapter.class);

    public PrestamoReactiveRepositoryAdapter(PrestamoReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Prestamo.class));
    }


}
