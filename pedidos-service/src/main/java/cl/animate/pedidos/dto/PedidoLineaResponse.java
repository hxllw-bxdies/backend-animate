package cl.animate.pedidos.dto;

/** Producto comprado, como lo muestra el historial del frontend. */
public record PedidoLineaResponse(
    String tipo, Long productoId, Integer tomo, String nombre, int precio, int cantidad) {}
