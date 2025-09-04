package co.com.pragma.crediya.gateways;

import reactor.core.publisher.Mono;

public interface UserGateway {

    Mono<Boolean> existUserByDocument(String documento);

    Mono<String> getDocumentoByCorreo(String correo);
}
