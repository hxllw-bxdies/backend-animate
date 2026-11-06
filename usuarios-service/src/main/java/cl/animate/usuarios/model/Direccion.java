package cl.animate.usuarios.model;

import jakarta.persistence.*;

/** Tabla direcciones: direcciones de despacho de cada usuario. */
@Entity
@Table(name = "direcciones")
public class Direccion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public Long usuarioId;

  @Column(nullable = false, length = 150)
  public String calle;

  @Column(length = 150)
  public String referencia;

  @Column(nullable = false, length = 80)
  public String comuna;

  @Column(nullable = false, length = 60)
  public String region;

  /** Dirección elegida para las compras. */
  public boolean seleccionada;

  public Direccion() {}
}
