package cl.animate.pedidos.dto;

import java.time.Instant;
import java.util.List;

/** Misma idea que los objetos de src/utils/ventas.js: comprador, tarjeta, dirección, productos y total. */
public record PedidoResponse(
    String id,
    Long usuarioId,
    String comprador,
    String estado,
    Instant fecha,
    String direccion,
    String tarjeta,
    List<PedidoLineaResponse> productos,
    int total,
    String mensaje) {}
