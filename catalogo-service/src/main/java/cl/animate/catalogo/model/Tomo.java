package cl.animate.catalogo.model;

import jakarta.persistence.*;

/** Tabla tomos: cada tomo de un manga tiene su propio precio, stock e imagen. */
@Entity
@Table(
    name = "tomos",
    uniqueConstraints = @UniqueConstraint(columnNames = {"manga_id", "numero"}))
public class Tomo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "manga_id")
  public Manga manga;

  /** Número del tomo (1, 2, 3...). En el frontend se llama "tomo". */
  @Column(nullable = false)
  public int numero;

  @Column(nullable = false)
  public int precio;

  @Column(nullable = false)
  public int stock;

  @Column(length = 500)
  public String img;

  public Tomo() {}

  public Tomo(Manga manga, int numero, int precio, int stock, String img) {
    this.manga = manga;
    this.numero = numero;
    this.precio = precio;
    this.stock = stock;
    this.img = img;
  }
}
