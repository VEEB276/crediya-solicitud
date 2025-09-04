package co.com.pragma.crediya.r2dbc;

import co.com.pragma.crediya.gateways.User;
import co.com.pragma.crediya.gateways.UserGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Repository
public class UserWebClientAdapter implements UserGateway {

    private final WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(UserWebClientAdapter.class);

    public UserWebClientAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Boolean> existUserByDocument(String documento) {
        return webClient.get()
                .uri("/api/v1/usuarios/documento/{documento}", documento)
                .retrieve()
                .bodyToMono(Void.class)
                .then(Mono.fromCallable(() -> true))
                .onErrorResume(e -> Mono.just(false)); 
    }

    @Override
    public Mono<User> getUserByCorreo(String correo) {
        return webClient.get()
                .uri("/api/v1/usuarios/correo/{correo}", correo)
                .retrieve()
                .bodyToMono(User.class)
                .doOnNext(doc -> log.info("Usuario obtenido por correo {}: {}", correo, doc))
                .onErrorResume(e -> {
                    log.error("Error al obtener usuario por correo {}", correo, e);
                    return Mono.empty();
                });
    }

}
