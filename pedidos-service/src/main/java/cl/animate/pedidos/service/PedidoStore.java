package cl.animate.pedidos.service;

import cl.animate.common.exception.ApiException;
import cl.animate.pedidos.dto.InventarioResponse;
import cl.animate.pedidos.dto.PedidoRequest;
import cl.animate.pedidos.model.Pedido;
import cl.animate.pedidos.model.PedidoLinea;
import cl.animate.pedidos.repository.PedidosRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operaciones sobre la base de Pedidos. Cada método es una transacción local. */
@Service
public class PedidoStore {
  private final PedidosRepository repo;

  public PedidoStore(PedidosRepository repo) {
    this.repo = repo;
  }

  /** Registra el pedido como PENDIENTE, o devuelve el existente si es un reintento. */
  @Transactional
  public Pedido iniciar(String id, Long usuarioId, String correo, PedidoRequest form) {
    String huella = hash(form.toString());
    Optional<Pedido> anterior = repo.findById(id);
    if (anterior.isPresent()) {
      Pedido p = anterior.get();
      if (!p.usuarioId.equals(usuarioId))
        throw new ApiException(403, "Ese pedido pertenece a otro usuario.");
      if (!p.huella.equals(huella))
        throw new ApiException(409, "Utiliza una clave nueva si cambias el contenido del pedido.");
      return p;
    }
    Pedido p = new Pedido();
    p.id = id;
    p.usuarioId = usuarioId;
    p.comprador = correo;
    p.huella = huella;
    p.direccion = form.direccion().trim();
    p.tarjeta = "**** " + form.tarjetaUltimos4();
    return repo.saveAndFlush(p);
  }

  /** Guarda las líneas con los precios que confirmó Catálogo y marca CONFIRMADO. */
  @Transactional
  public Pedido confirmar(String id, InventarioResponse comprobante) {
    Pedido p = repo.findById(id).orElseThrow();
    if (!p.estado.equals("PENDIENTE")) return p;
    p.items =
        comprobante.items().stream()
            .map(
                i ->
                    new PedidoLinea(
                        i.tipo(), i.productoId(), i.tomo(), i.nombre(), i.precio(), i.cantidad()))
            .collect(Collectors.toCollection(ArrayList::new));
    p.total = comprobante.subtotal();
    p.estado = "CONFIRMADO";
    p.mensaje = "Compra confirmada. Pago simulado.";
    return repo.saveAndFlush(p);
  }

  @Transactional
  public Pedido rechazar(String id, String motivo) {
    Pedido p = repo.findById(id).orElseThrow();
    p.estado = "RECHAZADO";
    p.mensaje = motivo;
    return repo.saveAndFlush(p);
  }

  public List<Pedido> mios(Long usuarioId) {
    return repo.findByUsuarioIdOrderByCreadoDesc(usuarioId);
  }

  public List<Pedido> todos() {
    return repo.findAllByOrderByCreadoDesc();
  }

  /** Un usuario solo puede ver sus pedidos; el admin puede ver cualquiera. */
  public Pedido obtener(String id, Jwt jwt) {
    Pedido p = repo.findById(id).orElseThrow(() -> new ApiException(404, "Pedido no encontrado."));
    if (!esAdmin(jwt) && !p.usuarioId.toString().equals(jwt.getSubject()))
      throw new ApiException(403, "No tienes acceso a ese pedido.");
    return p;
  }

  @Transactional
  public Pedido cambiarEstado(String id, String nuevo) {
    Pedido p = repo.findById(id).orElseThrow(() -> new ApiException(404, "Pedido no encontrado."));
    boolean valido =
        p.estado.equals("CONFIRMADO") && nuevo.equals("ENVIADO")
            || p.estado.equals("ENVIADO") && nuevo.equals("ENTREGADO");
    if (!valido) throw new ApiException(409, "Transición de estado no permitida.");
    p.estado = nuevo;
    return repo.save(p);
  }

  public static boolean esAdmin(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("roles");
    return roles != null && roles.contains("ADMIN");
  }

  private String hash(String texto) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
