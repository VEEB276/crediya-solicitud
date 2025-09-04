package co.com.pragma.crediya.security.jwt.filter;

import co.com.pragma.crediya.exception.BusinessException;
import co.com.pragma.crediya.security.jwt.provider.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtFilter implements WebFilter {

    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.contains("login")) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null) {
            return Mono.error(new Throwable("no token was found"));
        }
        if (!auth.startsWith("Bearer ")) {
            return Mono.error(new Throwable("invalid auth"));
        }

        String token = auth.replace("Bearer ", "");
        if (!jwtProvider.validate(token)) {
            return Mono.error(new BusinessException("invalid token"));
        }

        String email = jwtProvider.getSubject(token);
        String role = (String) jwtProvider.getClaims(token).get("roles");

        // Convertimos role a GrantedAuthority
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        Authentication authObj = new UsernamePasswordAuthenticationToken(email, null, authorities);

        // Pasamos el auth al contexto de seguridad
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authObj));
    }
}
