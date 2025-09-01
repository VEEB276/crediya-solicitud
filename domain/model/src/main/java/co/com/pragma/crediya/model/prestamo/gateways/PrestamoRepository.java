package co.com.pragma.crediya.model.prestamo.gateways;

import co.com.pragma.crediya.model.prestamo.Prestamo;
import reactor.core.publisher.Mono;

public interface PrestamoRepository {
    Mono<Prestamo> findById(Long id);
}
