package co.com.pragma.crediya.api;

import co.com.pragma.crediya.api.dto.CreateApplicationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperation(
            path = "/api/v1/solicitud",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST,
            beanClass = Handler.class,
            beanMethod = "listenSaveApplication",
            operation = @Operation(
                    operationId = "guardarSolicitud",
                    description = "Guarda una nueva solicitud",
                    requestBody = @RequestBody(
                            required = true,
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CreateApplicationDTO.class),
                                    examples = @ExampleObject(
                                            value = "{\"monto\":1500000.50,\"documentoIdentidad\":\"123456789\",\"email\":\"valen@gmail.com\",\"plazo\":1,\"idPrestamo\":1}"))),
                    responses = {
                            @ApiResponse(responseCode = "200", description = "Solicitud guardada correctamente"),
                            @ApiResponse(responseCode = "400", description = "Error de validación")
                    }
            )
    )
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitud"), handler::listenSaveApplication);
    }

    @Bean
    public RouterFunction<ServerResponse> pageApplication(Handler handler) {
        return route(GET("/api/v1/solicitudes/pendientes"), handler::listarPendientes);
    }
}
