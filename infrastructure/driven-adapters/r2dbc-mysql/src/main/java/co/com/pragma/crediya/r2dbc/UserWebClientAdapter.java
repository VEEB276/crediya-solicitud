package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.gateways.UserGateway;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import co.com.pragma.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.crediya.r2dbc.entities.SolicitudEntity;
import co.com.pragma.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Repository
public class UserWebClientAdapter implements UserGateway {

    private final WebClient webClient;

    public UserWebClientAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Boolean> existUserByDocument(String documento) {
        return webClient.get()
                .uri("/api/v1/usuarios/{documento}", documento)
                .retrieve()
                .bodyToMono(Void.class)
                .then(Mono.fromCallable(() -> true))
                .onErrorResume(e -> Mono.just(false)); 
    }

}
