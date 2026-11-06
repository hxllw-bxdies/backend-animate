package cl.animate.catalogo.dto;

/**
 * Línea que envía Pedidos por la llamada interna. tipo = MANGA o FIGURA; tomo solo aplica a MANGA.
 */
public record VentaItemRequest(String tipo, Long productoId, Integer tomo, int cantidad) {
  /** Clave única del producto, equivalente a crearClave() del carrito: MANGA-1-2, FIGURA-3. */
  public String clave() {
    return tomo == null ? tipo + "-" + productoId : tipo + "-" + productoId + "-" + tomo;
  }
}
