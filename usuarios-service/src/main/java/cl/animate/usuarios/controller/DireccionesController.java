package cl.animate.usuarios.controller;

import cl.animate.usuarios.dto.DireccionRequest;
import cl.animate.usuarios.dto.DireccionResponse;
import cl.animate.usuarios.service.DireccionesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** El dueño se toma siempre del JWT, nunca de la URL: nadie puede ver direcciones ajenas. */
@Tag(name = "Direcciones", description = "Direcciones de despacho del usuario con sesión")
@RestController
@RequestMapping("/api/direcciones")
public class DireccionesController {
  private final DireccionesService service;

  public DireccionesController(DireccionesService service) {
    this.service = service;
  }

  @Operation(summary = "Regiones válidas para el formulario.")
  @GetMapping("/regiones")
  public List<String> regiones() {
    return DireccionesService.REGIONES;
  }

  @Operation(summary = "Lista mis direcciones.")
  @GetMapping
  public List<DireccionResponse> listar(@AuthenticationPrincipal Jwt jwt) {
    return service.listar(id(jwt));
  }

  @Operation(summary = "Agrega una dirección.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DireccionResponse crear(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DireccionRequest r) {
    return service.crear(id(jwt), r);
  }

  @Operation(summary = "Marca una dirección como seleccionada para comprar.")
  @PutMapping("/{id}/seleccionar")
  public DireccionResponse seleccionar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    return service.seleccionar(id(jwt), id);
  }

  @Operation(summary = "Elimina una dirección.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
    service.eliminar(id(jwt), id);
  }

  private Long id(Jwt jwt) {
    return Long.valueOf(jwt.getSubject());
  }
}
