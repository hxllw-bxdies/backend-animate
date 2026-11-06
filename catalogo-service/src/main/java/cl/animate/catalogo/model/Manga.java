package cl.animate.catalogo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Tabla mangas. Cada manga tiene una lista de tomos (tabla tomos, relación 1 a N). */
@Entity
@Table(name = "mangas")
public class Manga {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 150)
  public String titulo;

  /** Título normalizado (minúsculas, sin tildes) para impedir mangas repetidos. */
  @Column(nullable = false, unique = true, length = 200)
  public String tituloClave;

  @Column(nullable = false, length = 40)
  public String genero;

  public int vistas;

  public LocalDate fechaAgregado = LocalDate.now();

  @Column(length = 2000)
  public String sinopsis;

  @Column(length = 500)
  public String imgPortada;

  /** false = está en la papelera (se puede restaurar). */
  public boolean activo = true;

  @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @OrderBy("numero ASC")
  public List<Tomo> tomos = new ArrayList<>();

  public Manga() {}
}
