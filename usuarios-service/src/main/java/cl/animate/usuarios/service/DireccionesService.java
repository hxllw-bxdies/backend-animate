package cl.animate.usuarios.service;

import cl.animate.common.exception.ApiException;
import cl.animate.usuarios.dto.DireccionRequest;
import cl.animate.usuarios.dto.DireccionResponse;
import cl.animate.usuarios.model.Direccion;
import cl.animate.usuarios.repository.DireccionesRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Direcciones de despacho. Cada usuario solo puede ver y modificar las suyas. */
@Service
public class DireccionesService {
  /** Mismas regiones que el arreglo "regiones" de src/utils/direcciones.js. */
  public static final List<String> REGIONES =
      List.of(
          "Arica y Parinacota", "Tarapacá", "Antofagasta", "Atacama", "Coquimbo", "Valparaíso",
          "Metropolitana de Santiago", "O'Higgins", "Maule", "Ñuble", "Biobío", "La Araucanía",
          "Los Ríos", "Los Lagos", "Aysén", "Magallanes");

  private final DireccionesRepository repo;

  public DireccionesService(DireccionesRepository repo) {
    this.repo = repo;
  }

  public static DireccionResponse vista(Direccion d) {
    String ref = d.referencia == null || d.referencia.isBlank() ? "" : ", " + d.referencia;
    String texto = d.calle + ref + ", " + d.comuna + ", " + d.region;
    return new DireccionResponse(
        d.id, d.calle, d.referencia, d.comuna, d.region, d.seleccionada, texto);
  }

  public List<DireccionResponse> listar(Long usuarioId) {
    return repo.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
        .map(DireccionesService::vista)
        .toList();
  }

  @Transactional
  public DireccionResponse crear(Long usuarioId, DireccionRequest r) {
    if (!REGIONES.contains(r.region())) throw new ApiException(400, "Seleccione una región válida");
    Direccion d = new Direccion();
    d.usuarioId = usuarioId;
    d.calle = r.calle().trim();
    d.referencia = r.referencia() == null ? "" : r.referencia().trim();
    d.comuna = r.comuna().trim();
    d.region = r.region();
    // La primera dirección queda seleccionada automáticamente.
    d.seleccionada = repo.findByUsuarioIdOrderByIdAsc(usuarioId).isEmpty();
    return vista(repo.save(d));
  }

  @Transactional
  public DireccionResponse seleccionar(Long usuarioId, Long id) {
    Direccion elegida = buscar(usuarioId, id);
    List<Direccion> todas = repo.findByUsuarioIdOrderByIdAsc(usuarioId);
    todas.forEach(d -> d.seleccionada = d.id.equals(elegida.id));
    repo.saveAll(todas);
    return vista(elegida);
  }

  @Transactional
  public void eliminar(Long usuarioId, Long id) {
    Direccion d = buscar(usuarioId, id);
    boolean eraSeleccionada = d.seleccionada;
    repo.delete(d);
    repo.flush();
    if (eraSeleccionada) {
      // Igual que el frontend: si se borra la seleccionada, se elige la primera que quede.
      repo.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
          .findFirst()
          .ifPresent(
              primera -> {
                primera.seleccionada = true;
                repo.save(primera);
              });
    }
  }

  private Direccion buscar(Long usuarioId, Long id) {
    return repo.findByIdAndUsuarioId(id, usuarioId)
        .orElseThrow(() -> new ApiException(404, "Dirección no encontrada."));
  }
}
