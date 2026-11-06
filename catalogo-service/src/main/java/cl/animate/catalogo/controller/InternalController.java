package cl.animate.catalogo.controller;

import cl.animate.catalogo.dto.VentaItemRequest;
import cl.animate.catalogo.dto.VentaResponse;
import cl.animate.catalogo.service.InventarioService;
import cl.animate.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Hidden;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint INTERNO: solo lo llama el microservicio de Pedidos. No está publicado en el Gateway ni
 * en Swagger (@Hidden) y exige la clave compartida X-Internal-Key.
 */
@Hidden
@RestController
@RequestMapping("/internal/ventas")
public class InternalController {
  private final InventarioService service;
  private final String clave;

  public InternalController(InventarioService service, @Value("${app.internal-key}") String clave) {
    this.service = service;
    this.clave = clave;
  }

  @PostMapping("/{pedidoId}")
  public VentaResponse vender(
      @PathVariable UUID pedidoId,
      @RequestHeader(value = "X-Internal-Key", defaultValue = "") String recibida,
      @RequestBody List<VentaItemRequest> items) {
    // MessageDigest.isEqual compara en tiempo constante (no revela cuántos caracteres coinciden).
    if (!MessageDigest.isEqual(
        clave.getBytes(StandardCharsets.UTF_8), recibida.getBytes(StandardCharsets.UTF_8)))
      throw new ApiException(403, "Servicio no autorizado.");
    return service.vender(pedidoId.toString(), items);
  }
}
