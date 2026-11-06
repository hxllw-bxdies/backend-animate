package cl.animate.catalogo.service;

import cl.animate.catalogo.dto.MangaRequest;
import cl.animate.catalogo.dto.MangaResponse;
import cl.animate.catalogo.dto.MangaUpdateRequest;
import cl.animate.catalogo.dto.PrecioStockRequest;
import cl.animate.catalogo.dto.TomoRequest;
import cl.animate.catalogo.dto.TomoResponse;
import cl.animate.catalogo.model.Manga;
import cl.animate.catalogo.model.Tomo;
import cl.animate.catalogo.repository.MangasRepository;
import cl.animate.common.exception.ApiException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de negocio de mangas y tomos. Reemplaza a src/utils/catalogoMangas.js, que guardaba todo
 * en localStorage.
 */
@Service
public class MangasService {
  private final MangasRepository repo;

  public MangasService(MangasRepository repo) {
    this.repo = repo;
  }

  /** Entidad → DTO. */
  public static MangaResponse vista(Manga m) {
    return new MangaResponse(
        m.id,
        m.titulo,
        m.genero,
        m.vistas,
        m.fechaAgregado == null ? null : m.fechaAgregado.toString(),
        m.sinopsis,
        m.imgPortada,
        m.tomos.stream().map(t -> new TomoResponse(t.numero, t.precio, t.stock, t.img)).toList());
  }

  public List<MangaResponse> listar() {
    return repo.findByActivoTrueOrderByIdAsc().stream().map(MangasService::vista).toList();
  }

  public List<MangaResponse> papelera() {
    return repo.findByActivoFalseOrderByIdAsc().stream().map(MangasService::vista).toList();
  }

  public MangaResponse obtener(Long id) {
    return vista(
        repo.findById(id)
            .filter(m -> m.activo)
            .orElseThrow(() -> new ApiException(404, "Manga no encontrado.")));
  }

  @Transactional
  public MangaResponse crear(MangaRequest r) {
    String clave = Textos.normalizar(r.titulo());
    validarTituloLibre(clave, -1L);
    Manga m = new Manga();
    m.titulo = r.titulo().trim();
    m.tituloClave = clave;
    m.genero = r.genero().trim();
    m.sinopsis = r.sinopsis().trim();
    m.imgPortada = r.imgPortada().trim();
    m.tomos.add(new Tomo(m, 1, r.precio(), r.stock(), m.imgPortada));
    return vista(repo.saveAndFlush(m));
  }

  @Transactional
  public MangaResponse editar(Long id, MangaUpdateRequest r) {
    Manga m = activo(id);
    String clave = Textos.normalizar(r.titulo());
    validarTituloLibre(clave, id);
    m.titulo = r.titulo().trim();
    m.tituloClave = clave;
    m.genero = r.genero().trim();
    m.sinopsis = r.sinopsis().trim();
    m.imgPortada = r.imgPortada().trim();
    return vista(repo.saveAndFlush(m));
  }

  /** No borra la fila: la manda a la papelera (activo = false), como pidió el cliente. */
  @Transactional
  public void eliminar(Long id) {
    Manga m = activo(id);
    m.activo = false;
    m.tituloClave = Textos.clavePapelera(m.tituloClave, m.id);
    repo.save(m);
  }

  @Transactional
  public MangaResponse restaurar(Long id) {
    Manga m =
        repo.bloquear(id)
            .filter(x -> !x.activo)
            .orElseThrow(() -> new ApiException(404, "Ese manga no está en la papelera."));
    String clave = Textos.claveOriginal(m.tituloClave);
    validarTituloLibre(clave, id);
    m.activo = true;
    m.tituloClave = clave;
    return vista(repo.saveAndFlush(m));
  }

  @Transactional
  public MangaResponse agregarTomo(Long id, TomoRequest r) {
    Manga m = activo(id);
    int siguiente = m.tomos.stream().mapToInt(t -> t.numero).max().orElse(0) + 1;
    m.tomos.add(new Tomo(m, siguiente, r.precio(), r.stock(), r.img().trim()));
    return vista(repo.saveAndFlush(m));
  }

  @Transactional
  public MangaResponse cambiarPrecioYStock(Long id, int numero, PrecioStockRequest r) {
    Manga m = activo(id);
    Tomo t = tomo(m, numero);
    t.precio = r.precio();
    t.stock = r.stock();
    return vista(repo.saveAndFlush(m));
  }

  @Transactional
  public MangaResponse eliminarTomo(Long id, int numero) {
    Manga m = activo(id);
    m.tomos.remove(tomo(m, numero)); // orphanRemoval borra la fila de la tabla tomos
    return vista(repo.saveAndFlush(m));
  }

  private void validarTituloLibre(String clave, Long id) {
    if (repo.existsByTituloClaveAndIdNot(clave, id))
      throw new ApiException(409, "Ya existe un manga con ese título.");
  }

  private Manga activo(Long id) {
    return repo.bloquear(id)
        .filter(m -> m.activo)
        .orElseThrow(() -> new ApiException(404, "Manga no encontrado."));
  }

  private Tomo tomo(Manga m, int numero) {
    return m.tomos.stream()
        .filter(t -> t.numero == numero)
        .findFirst()
        .orElseThrow(() -> new ApiException(404, "Tomo no encontrado."));
  }
}
