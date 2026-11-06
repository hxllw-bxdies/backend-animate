package cl.animate.pedidos.config;

import cl.animate.common.security.SecuritySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Comprar y ver el historial propio requiere sesión; ver todas las ventas y cambiar estados, ADMIN. */
@Configuration
public class SecurityConfig {
  @Bean
  JwtDecoder decoder(@Value("${app.jwks}") String jwks, @Value("${app.issuer}") String issuer) {
    return SecuritySupport.decoder(jwks, issuer);
  }

  @Bean
  SecurityFilterChain security(HttpSecurity h) throws Exception {
    SecuritySupport.base(h);
    h.authorizeHttpRequests(
        a ->
            a.requestMatchers(
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pedidos")
                .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/estado")
                .hasRole("ADMIN")
                .requestMatchers("/api/pedidos/**")
                .hasAnyRole("USUARIO", "ADMIN")
                .anyRequest()
                .denyAll());
    return h.build();
  }
}
