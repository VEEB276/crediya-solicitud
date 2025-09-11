package co.com.pragma.crediya.sqs.sender;

import co.com.pragma.crediya.evento.ApplicationPublisher;
import co.com.pragma.crediya.evento.UpdateApplicationEvent;
import co.com.pragma.crediya.exception.ValidationException;
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
                    payload.put("status", event.status());
                    payload.put("emailClient", event.emailClient());
                    payload.put("identityDocument", event.identityDocument());
                    payload.put("loanAmount", event.loanAmount());
                    payload.put("loanType", event.loanType());
                    payload.put("customMessage", event.customMessage());

                    // Se construye el JSON y se devuelve como String
                    return objectMapper.writeValueAsString(payload);
                })
                .onErrorMap(JsonProcessingException.class, e -> {
                    log.error("Error al convertir el payload a JSON", e);
                    return new ValidationException("Error al serializar el mensaje");
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
