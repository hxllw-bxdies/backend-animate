package cl.animate.catalogo.dto;

/** Línea del comprobante con el nombre y el precio VIGENTE al momento de la compra. */
public record VentaLineaResponse(
    String tipo, Long productoId, Integer tomo, String nombre, int precio, int cantidad) {}
