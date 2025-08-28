package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.estado.Estado;
import co.com.pragma.crediya.model.estado.gateways.EstadoRepository;
import co.com.pragma.crediya.model.prestamo.Prestamo;
import co.com.pragma.crediya.model.prestamo.gateways.PrestamoRepository;
import co.com.pragma.crediya.r2dbc.entities.EstadoEntity;
import co.com.pragma.crediya.r2dbc.entities.PrestamoEntity;
import co.com.pragma.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class EstadoReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Estado,
        EstadoEntity,
        Long,
        EstadoReactiveRepository
> implements EstadoRepository
{
    private static final Logger log = LoggerFactory.getLogger(EstadoReactiveRepositoryAdapter.class);

    public EstadoReactiveRepositoryAdapter(EstadoReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Estado.class));
    }


    @Override
    public Mono<Estado> findBySigla(String sigla) {
        return repository.findBySigla(sigla)
                .doOnError(error -> log.error("Error buscando el estado por sigla {}: {}", sigla, error.getMessage(), error));
    }
}
