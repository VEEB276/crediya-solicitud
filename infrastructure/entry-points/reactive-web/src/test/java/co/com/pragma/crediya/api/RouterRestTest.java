package co.com.pragma.crediya.api;

import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
class RouterRestTest {

}
