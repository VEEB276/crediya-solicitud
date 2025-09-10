package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.evento.ApplicationPublisher;
import co.com.pragma.crediya.evento.UpdateApplicationEvent;
import co.com.pragma.crediya.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements ApplicationPublisher {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    public Mono<String> send(UpdateApplicationEvent event) {

        return Mono.fromCallable(() -> {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("requestId", event.requestId());
                    payload.put("status", "APROBADA");
                    payload.put("emailClient", event.emailClient());
                    payload.put("identityDocument", event.identityDocument());
                    payload.put("loanAmount", 5000000);
                    payload.put("loanType", "Libranza");
                    payload.put("customMessage", "Su desembolso estará disponible en las próximas 24 horas.");

                    try {
                        // Construimos el JSON y lo devolvemos como un String
                        return objectMapper.writeValueAsString(payload);
                    } catch (JsonProcessingException e) {
                        // Manejamos la excepción de forma reactiva, propagándola como un error en el Mono
                        log.error("Error al convertir el payload a JSON: {}", e.getMessage());
                        throw new RuntimeException("Error al serializar el mensaje", e);
                    }
                })
                .flatMap(message -> Mono.fromFuture(client.sendMessage(buildRequest(message))))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }
}
