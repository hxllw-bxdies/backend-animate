package cl.animate.pedidos.controller;

import cl.animate.pedidos.dto.EstadoRequest;
import cl.animate.pedidos.dto.PedidoRequest;
import cl.animate.pedidos.dto.PedidoResponse;
import cl.animate.pedidos.mapper.PedidoMapper;
import cl.animate.pedidos.service.PedidoStore;
import cl.animate.pedidos.service.PedidoWorkflow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pedidos", description = "Compras, historial del usuario y ventas (ADMIN)")
@RestController
@RequestMapping("/api/pedidos")
public class PedidosController {
  private final PedidoWorkflow workflow;
  private final PedidoStore store;

  public PedidosController(PedidoWorkflow workflow, PedidoStore store) {
    this.workflow = workflow;
    this.store = store;
  }

  @Operation(summary = "Confirma una compra. El usuario se toma del JWT, no del cuerpo.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PedidoResponse crear(
      @Parameter(description = "UUID nuevo por cada compra (ej: crypto.randomUUID())")
          @RequestHeader("Idempotency-Key")
          UUID clave,
      @Valid @RequestBody PedidoRequest form,
      @AuthenticationPrincipal Jwt jwt) {
    return PedidoMapper.toResponse(
        workflow.comprar(
            clave.toString(), Long.valueOf(jwt.getSubject()), jwt.getClaimAsString("correo"), form));
  }

  @Operation(summary = "Mis compras (PerfilUsuario).")
  @GetMapping("/mios")
  public List<PedidoResponse> mios(@AuthenticationPrincipal Jwt jwt) {
    return store.mios(Long.valueOf(jwt.getSubject())).stream()
        .map(PedidoMapper::toResponse)
        .toList();
  }

  @Operation(summary = "Todas las ventas (PerfilAdmin, solo ADMIN).")
  @GetMapping
  public List<PedidoResponse> todos() {
    return store.todos().stream().map(PedidoMapper::toResponse).toList();
  }

  @Operation(summary = "Detalle de un pedido (dueño o ADMIN).")
  @GetMapping("/{id}")
  public PedidoResponse obtener(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
    return PedidoMapper.toResponse(store.obtener(id.toString(), jwt));
  }

  @Operation(summary = "Cambia el estado: CONFIRMADO → ENVIADO → ENTREGADO (ADMIN).")
  @PutMapping("/{id}/estado")
  public PedidoResponse estado(@PathVariable UUID id, @Valid @RequestBody EstadoRequest e) {
    return PedidoMapper.toResponse(store.cambiarEstado(id.toString(), e.estado()));
  }
}
