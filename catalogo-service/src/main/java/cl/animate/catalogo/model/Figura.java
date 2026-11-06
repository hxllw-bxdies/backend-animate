package cl.animate.catalogo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Tabla figuras. La galería de imágenes se guarda en la tabla figura_galeria. */
@Entity
@Table(name = "figuras")
public class Figura {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 150)
  public String nombre;

  @Column(nullable = false, unique = true, length = 200)
  public String nombreClave;

  @Column(nullable = false, length = 100)
  public String proveniencia;

  @Column(nullable = false, length = 60)
  public String tipoFigura;

  @Column(nullable = false)
  public int precio;

  @Column(nullable = false)
  public int stock;

  public int vistas;

  public LocalDate fechaAgregado = LocalDate.now();

  @Column(length = 500)
  public String imgPortada;

  public boolean activo = true;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "figura_galeria", joinColumns = @JoinColumn(name = "figura_id"))
  @OrderColumn(name = "orden")
  @Column(name = "ruta", length = 500)
  public List<String> galeria = new ArrayList<>();

  public Figura() {}
}
