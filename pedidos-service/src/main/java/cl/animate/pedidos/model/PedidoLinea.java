package cl.animate.pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Línea de un pedido (tabla pedido_items). Guarda nombre y precio AL MOMENTO DE LA COMPRA: si
 * después cambia el catálogo, el historial no cambia. productoId es solo una referencia a Catálogo;
 * no hay relación JPA entre bases de distintos microservicios.
 */
@Embeddable
public class PedidoLinea {
  @Column(length = 10)
  public String tipo;

  public Long productoId;
  public Integer tomo;

  @Column(length = 200)
  public String nombre;

  public int precio;
  public int cantidad;

  protected PedidoLinea() {}

  public PedidoLinea(
      String tipo, Long productoId, Integer tomo, String nombre, int precio, int cantidad) {
    this.tipo = tipo;
    this.productoId = productoId;
    this.tomo = tomo;
    this.nombre = nombre;
    this.precio = precio;
    this.cantidad = cantidad;
  }
}
