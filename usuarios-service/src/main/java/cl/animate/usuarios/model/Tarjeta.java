package cl.animate.usuarios.model;

import jakarta.persistence.*;

/** Tabla tarjetas: solo se guardan los últimos 4 dígitos y el propietario (nunca número ni clave). */
@Entity
@Table(name = "tarjetas")
public class Tarjeta {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public Long usuarioId;

  @Column(nullable = false, length = 4)
  public String ultimos4;

  @Column(nullable = false, length = 100)
  public String propietario;

  public boolean seleccionada;

  public Tarjeta() {}
}
