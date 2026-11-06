package cl.animate.usuarios.controller;

import cl.animate.usuarios.dto.TarjetaRequest;
import cl.animate.usuarios.dto.TarjetaResponse;
import cl.animate.usuarios.service.TarjetasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tarjetas", description = "Tarjetas del usuario con sesión (solo últimos 4 dígitos)")
@RestController
@RequestMapping("/api/tarjetas")
public class TarjetasController {
  private final TarjetasService service;

  public TarjetasController(TarjetasService service) {
    this.service = service;
  }

  @Operation(summary = "Lista mis tarjetas.")
  @GetMapping
  public List<TarjetaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
    return service.listar(id(jwt));
  }

  @Operation(summary = "Agrega una tarjeta (se guardan solo los últimos 4 dígitos).")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TarjetaResponse crear(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TarjetaRequest r) {
    return service.crear(id(jwt), r);
  }

  @Operation(summary = "Marca una tarjeta como seleccionada.")
  @PutMapping("/{id}/seleccionar")
  public TarjetaResponse seleccionar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    return service.seleccionar(id(jwt), id);
  }

  @Operation(summary = "Elimina una tarjeta.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    service.eliminar(id(jwt), id);
  }

  private Long id(Jwt jwt) {
    return Long.valueOf(jwt.getSubject());
  }
}
