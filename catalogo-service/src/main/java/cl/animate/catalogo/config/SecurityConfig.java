package cl.animate.catalogo.config;

import cl.animate.common.security.SecuritySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Catálogo público para leer; crear, editar, eliminar y ver la papelera solo para ADMIN. */
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
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/internal/ventas/**")
                .permitAll()
                // La papelera es de administración aunque sea un GET (va antes de la regla pública).
                .requestMatchers("/api/mangas/papelera", "/api/figuras/papelera")
                .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/mangas/**", "/api/figuras/**")
                .permitAll()
                .requestMatchers("/api/mangas/**", "/api/figuras/**")
                .hasRole("ADMIN")
                .anyRequest()
                .denyAll());
    return h.build();
  }
}
