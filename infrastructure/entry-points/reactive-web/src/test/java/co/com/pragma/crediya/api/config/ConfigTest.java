package co.com.pragma.crediya.api.config;

import co.com.pragma.crediya.api.Handler;
import co.com.pragma.crediya.api.RouterRest;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

}