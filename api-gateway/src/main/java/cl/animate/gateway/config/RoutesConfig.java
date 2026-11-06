package cl.animate.gateway.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * React solo conoce http://localhost:8080/api. El Gateway elige el microservicio según el camino y
 * conserva la ruta (GET /api/mangas/5 → http://localhost:8082/api/mangas/5).
 *
 * <p>El Gateway ENRUTA; la validación del JWT, los permisos y las reglas de negocio las hace cada
 * microservicio. /internal/** no se publica aquí.
 */
@Configuration
public class RoutesConfig {
  @Bean
  RouteLocator rutas(
      RouteLocatorBuilder b,
      @Value("${app.usuarios-url}") String usuarios,
      @Value("${app.catalogo-url}") String catalogo,
      @Value("${app.pedidos-url}") String pedidos) {
    return b.routes()
        .route(
            "usuarios",
            r ->
                r.path("/api/auth/**", "/api/usuarios/**", "/api/direcciones/**", "/api/tarjetas/**")
                    .uri(usuarios))
        .route("catalogo", r -> r.path("/api/mangas/**", "/api/figuras/**").uri(catalogo))
        .route("pedidos", r -> r.path("/api/pedidos/**").uri(pedidos))
        // Documentación OpenAPI de cada servicio para el Swagger del Gateway.
        .route(
            "docs-usuarios",
            r -> r.path("/openapi/usuarios").filters(f -> f.setPath("/v3/api-docs")).uri(usuarios))
        .route(
            "docs-catalogo",
            r -> r.path("/openapi/catalogo").filters(f -> f.setPath("/v3/api-docs")).uri(catalogo))
        .route(
            "docs-pedidos",
            r -> r.path("/openapi/pedidos").filters(f -> f.setPath("/v3/api-docs")).uri(pedidos))
        .build();
  }

  /**
   * CORS: React (localhost:5173) y el Gateway (localhost:8080) son orígenes distintos. Con
   * credenciales (cookie de renovación) hay que indicar orígenes concretos, nunca "*".
   */
  @Bean
  CorsWebFilter cors(@Value("${app.allowed-origins}") String origenes) {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).toList());
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(
        List.of("Content-Type", "Authorization", "Idempotency-Key", "X-Requested-With"));
    c.setAllowCredentials(true);
    c.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return new CorsWebFilter(source);
  }
}
