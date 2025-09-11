package co.com.pragma.crediya.evento;

import reactor.core.publisher.Mono;

public interface ApplicationPublisher {

    Mono<String> send(UpdateApplicationEvent event);
}
