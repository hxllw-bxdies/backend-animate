package cl.animate.catalogo.model;

import jakarta.persistence.*;

/**
 * Tabla ventas_inventario: comprobante de cada descuento de stock, identificado por el id del
 * pedido. Si Pedidos reintenta la misma compra, se devuelve este comprobante sin descontar otra vez
 * (idempotencia).
 */
@Entity
@Table(name = "ventas_inventario")
public class Venta {
  @Id
  @Column(length = 36)
  public String pedidoId;

  /** Resumen de los productos pedidos, para detectar si reutilizan el id con otro contenido. */
  @Column(nullable = false, length = 4096)
  public String huella;

  @Lob
  @Column(nullable = false)
  public String comprobante;

  public Venta() {}

  public Venta(String pedidoId, String huella, String comprobante) {
    this.pedidoId = pedidoId;
    this.huella = huella;
    this.comprobante = comprobante;
  }
}
