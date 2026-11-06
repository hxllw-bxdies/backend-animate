package cl.animate.usuarios.service;

import cl.animate.common.exception.ApiException;
import cl.animate.usuarios.dto.TarjetaRequest;
import cl.animate.usuarios.dto.TarjetaResponse;
import cl.animate.usuarios.model.Tarjeta;
import cl.animate.usuarios.repository.TarjetasRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tarjetas (pago simulado). Solo se guardan los últimos 4 dígitos, como en resumirTarjeta(). */
@Service
public class TarjetasService {
  private final TarjetasRepository repo;

  public TarjetasService(TarjetasRepository repo) {
    this.repo = repo;
  }

  public static TarjetaResponse vista(Tarjeta t) {
    return new TarjetaResponse(
        t.id,
        t.ultimos4,
        t.propietario,
        t.seleccionada,
        "**** **** **** " + t.ultimos4 + " (" + t.propietario + ")");
  }

  public List<TarjetaResponse> listar(Long usuarioId) {
    return repo.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
        .map(TarjetasService::vista)
        .toList();
  }

  @Transactional
  public TarjetaResponse crear(Long usuarioId, TarjetaRequest r) {
    String soloNumero = r.numero().replaceAll("\\s", "");
    Tarjeta t = new Tarjeta();
    t.usuarioId = usuarioId;
    t.ultimos4 = soloNumero.substring(soloNumero.length() - 4);
    t.propietario = r.propietario().trim();
    t.seleccionada = repo.findByUsuarioIdOrderByIdAsc(usuarioId).isEmpty();
    // r.numero() completo y r.clave() NO se guardan en ninguna parte.
    return vista(repo.save(t));
  }

  @Transactional
  public TarjetaResponse seleccionar(Long usuarioId, Long id) {
    Tarjeta elegida = buscar(usuarioId, id);
    List<Tarjeta> todas = repo.findByUsuarioIdOrderByIdAsc(usuarioId);
    todas.forEach(t -> t.seleccionada = t.id.equals(elegida.id));
    repo.saveAll(todas);
    return vista(elegida);
  }

  @Transactional
  public void eliminar(Long usuarioId, Long id) {
    Tarjeta t = buscar(usuarioId, id);
    boolean eraSeleccionada = t.seleccionada;
    repo.delete(t);
    repo.flush();
    if (eraSeleccionada) {
      repo.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
          .findFirst()
          .ifPresent(
              primera -> {
                primera.seleccionada = true;
                repo.save(primera);
              });
    }
  }

  private Tarjeta buscar(Long usuarioId, Long id) {
    return repo.findByIdAndUsuarioId(id, usuarioId)
        .orElseThrow(() -> new ApiException(404, "Tarjeta no encontrada."));
  }
}
