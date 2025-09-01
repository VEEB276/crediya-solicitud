package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.model.estado.Estado;
import co.com.pragma.crediya.r2dbc.entities.EstadoEntity;
import co.com.pragma.crediya.r2dbc.entities.PrestamoEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EstadoReactiveRepository extends ReactiveCrudRepository<EstadoEntity, Long>, ReactiveQueryByExampleExecutor<EstadoEntity> {

    Mono<Estado> findBySigla(String sigla);

}
