package co.com.pragma.crediya.evento;

public record UpdateApplicationEvent(
        String requestId,
        String status,
        String emailClient,
        String identityDocument,
        Long loanAmount,
        String loanType,
        String customMessage
) {}
