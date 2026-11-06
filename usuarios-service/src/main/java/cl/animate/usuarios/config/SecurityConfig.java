package cl.animate.usuarios.config;

import cl.animate.common.security.SecuritySupport;
import cl.animate.usuarios.security.JwtKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/** Reglas de acceso por ruta y rol. Spring Security las revisa antes de llegar al controlador. */
@Configuration
public class SecurityConfig {
  @Bean
  BCryptPasswordEncoder passwords() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtDecoder decoder(JwtKeys keys, @Value("${app.issuer}") String issuer) throws Exception {
    return keys.decoder(issuer);
  }

  @Bean
  SecurityFilterChain security(HttpSecurity h) throws Exception {
    SecuritySupport.base(h);
    h.authorizeHttpRequests(
        a ->
            a.requestMatchers(
                    "/api/auth/login",
                    "/api/auth/registro",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/auth/jwks",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health")
                .permitAll()
                // El propio perfil: cualquier usuario con sesión (va ANTES de /api/usuarios/**).
                .requestMatchers("/api/usuarios/me")
                .hasAnyRole("USUARIO", "ADMIN")
                // Gestión de usuarios: solo administradores.
                .requestMatchers("/api/usuarios/**")
                .hasRole("ADMIN")
                .requestMatchers("/api/direcciones/**", "/api/tarjetas/**")
                .hasAnyRole("USUARIO", "ADMIN")
                .anyRequest()
                .authenticated());
    return h.build();
  }
}
