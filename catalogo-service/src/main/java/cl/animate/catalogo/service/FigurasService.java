package cl.animate.catalogo.service;

import cl.animate.catalogo.dto.FiguraRequest;
import cl.animate.catalogo.dto.FiguraResponse;
import cl.animate.catalogo.dto.PrecioStockRequest;
import cl.animate.catalogo.model.Figura;
import cl.animate.catalogo.repository.FigurasRepository;
import cl.animate.common.exception.ApiException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de negocio de figuras. Reemplaza a src/utils/catalogoFiguras.js. */
@Service
public class FigurasService {
  private final FigurasRepository repo;

  public FigurasService(FigurasRepository repo) {
    this.repo = repo;
  }

  public static FiguraResponse vista(Figura f) {
    return new FiguraResponse(
        f.id,
        f.nombre,
        f.proveniencia,
        f.tipoFigura,
        f.stock,
        f.precio,
        f.vistas,
        f.fechaAgregado == null ? null : f.fechaAgregado.toString(),
        f.imgPortada,
        List.copyOf(f.galeria));
  }

  public List<FiguraResponse> listar() {
    return repo.findByActivoTrueOrderByIdAsc().stream().map(FigurasService::vista).toList();
  }

  public List<FiguraResponse> papelera() {
    return repo.findByActivoFalseOrderByIdAsc().stream().map(FigurasService::vista).toList();
  }

  public FiguraResponse obtener(Long id) {
    return vista(
        repo.findById(id)
            .filter(f -> f.activo)
            .orElseThrow(() -> new ApiException(404, "Figura no encontrada.")));
  }

  /** Crea (id null) o edita (id de la ruta) una figura. */
  @Transactional
  public FiguraResponse guardar(Long id, FiguraRequest r) {
    String clave = Textos.normalizar(r.nombre());
    if (repo.existsByNombreClaveAndIdNot(clave, id == null ? -1L : id))
      throw new ApiException(409, "Ya existe una figura con ese nombre.");
    Figura f = id == null ? new Figura() : activa(id);
    f.nombre = r.nombre().trim();
    f.nombreClave = clave;
    f.proveniencia = r.proveniencia().trim();
    f.tipoFigura = r.tipoFigura().trim();
    f.precio = r.precio();
    f.stock = r.stock();
    f.imgPortada = r.imgPortada().trim();
    return vista(repo.saveAndFlush(f));
  }

  @Transactional
  public FiguraResponse cambiarPrecioYStock(Long id, PrecioStockRequest r) {
    Figura f = activa(id);
    f.precio = r.precio();
    f.stock = r.stock();
    return vista(repo.saveAndFlush(f));
  }

  @Transactional
  public void eliminar(Long id) {
    Figura f = activa(id);
    f.activo = false;
    f.nombreClave = Textos.clavePapelera(f.nombreClave, f.id);
    repo.save(f);
  }

  @Transactional
  public FiguraResponse restaurar(Long id) {
    Figura f =
        repo.bloquear(id)
            .filter(x -> !x.activo)
            .orElseThrow(() -> new ApiException(404, "Esa figura no está en la papelera."));
    String clave = Textos.claveOriginal(f.nombreClave);
    if (repo.existsByNombreClaveAndIdNot(clave, id))
      throw new ApiException(409, "Ya existe una figura activa con ese nombre.");
    f.activo = true;
    f.nombreClave = clave;
    return vista(repo.saveAndFlush(f));
  }

  @Transactional
  public FiguraResponse agregarImagen(Long id, String ruta) {
    Figura f = activa(id);
    String limpia = ruta.trim();
    if (f.galeria.contains(limpia) || limpia.equals(f.imgPortada))
      throw new ApiException(409, "Esa imagen ya está en la galería.");
    f.galeria.add(limpia);
    return vista(repo.saveAndFlush(f));
  }

  /** La imagen de portada no se puede quitar de la galería (igual que en el frontend). */
  @Transactional
  public FiguraResponse eliminarImagen(Long id, String ruta) {
    Figura f = activa(id);
    if (ruta.equals(f.imgPortada))
      throw new ApiException(409, "No puedes eliminar la imagen de portada.");
    List<String> nueva = new ArrayList<>(f.galeria);
    if (!nueva.remove(ruta)) throw new ApiException(404, "Imagen no encontrada.");
    f.galeria.clear();
    f.galeria.addAll(nueva);
    return vista(repo.saveAndFlush(f));
  }

  private Figura activa(Long id) {
    return repo.bloquear(id)
        .filter(f -> f.activo)
        .orElseThrow(() -> new ApiException(404, "Figura no encontrada."));
  }
}
