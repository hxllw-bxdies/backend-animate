package cl.animate.pedidos.dto;

/** Línea del comprobante que devuelve Catálogo (mismos campos que VentaLineaResponse). */
public record InventarioLineaResponse(
    String tipo, Long productoId, Integer tomo, String nombre, int precio, int cantidad) {}
