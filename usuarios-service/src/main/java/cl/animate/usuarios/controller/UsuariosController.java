package cl.animate.usuarios.controller;

import cl.animate.usuarios.dto.PerfilUpdateRequest;
import cl.animate.usuarios.dto.UsuarioAdminRequest;
import cl.animate.usuarios.dto.UsuarioResponse;
import cl.animate.usuarios.dto.UsuarioUpdateRequest;
import cl.animate.usuarios.service.UsuariosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuarios", description = "Perfil propio y CRUD de usuarios (ADMIN)")
@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {
  private final UsuariosService service;

  public UsuariosController(UsuariosService service) {
    this.service = service;
  }

  @Operation(summary = "Edita el perfil del usuario con sesión (cualquier rol).")
  @PutMapping("/me")
  public UsuarioResponse editarPerfil(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilUpdateRequest r) {
    return service.editarPerfil(Long.valueOf(jwt.getSubject()), r);
  }

  @Operation(summary = "Lista los usuarios activos (ADMIN).")
  @GetMapping
  public List<UsuarioResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Obtiene un usuario por id (ADMIN).")
  @GetMapping("/{id}")
  public UsuarioResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Crea un usuario eligiendo su rol (ADMIN).")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse crear(@Valid @RequestBody UsuarioAdminRequest r) {
    return service.crear(r);
  }

  @Operation(summary = "Edita nombre, apellido, correo y rol de un usuario (ADMIN).")
  @PutMapping("/{id}")
  public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest r) {
    return service.editar(id, r);
  }

  @Operation(summary = "Desactiva un usuario (borrado lógico, ADMIN).")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
    service.eliminar(id, Long.valueOf(jwt.getSubject()));
  }
}
