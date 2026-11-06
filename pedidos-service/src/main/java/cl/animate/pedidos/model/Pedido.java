package cl.animate.pedidos.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Tabla pedidos. usuarioId referencia a una cuenta del microservicio Usuarios. */
@Entity
@Table(name = "pedidos")
public class Pedido {
  /** El id es la Idempotency-Key que envía React (UUID). */
  @Id
  @Column(length = 36)
  public String id;

  @Column(nullable = false)
  public Long usuarioId;

  /** Correo del comprador tomado del JWT, para mostrarlo en el panel admin. */
  @Column(length = 120)
  public String comprador;

  /** Hash del contenido, para detectar si se reutiliza la misma clave con otros datos. */
  @Column(length = 64)
  public String huella;

  /** PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO, o RECHAZADO. */
  @Column(length = 20)
  public String estado = "PENDIENTE";

  public Instant creado = Instant.now();

  @Column(length = 400)
  public String direccion;

  /** Tarjeta enmascarada: "**** 1234". */
  @Column(length = 20)
  public String tarjeta;

  public int total;

  @Column(length = 300)
  public String mensaje;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "pedido_items", joinColumns = @JoinColumn(name = "pedido_id"))
  public List<PedidoLinea> items = new ArrayList<>();

  public Pedido() {}
}
