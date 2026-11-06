package cl.animate.catalogo.service;

import cl.animate.catalogo.dto.VentaItemRequest;
import cl.animate.catalogo.dto.VentaLineaResponse;
import cl.animate.catalogo.dto.VentaResponse;
import cl.animate.catalogo.model.Figura;
import cl.animate.catalogo.model.Tomo;
import cl.animate.catalogo.model.Venta;
import cl.animate.catalogo.repository.FigurasRepository;
import cl.animate.catalogo.repository.TomosRepository;
import cl.animate.catalogo.repository.VentasRepository;
import cl.animate.common.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Descuento de stock de una compra. Reemplaza a descontarStock() de src/utils/inventario.js.
 *
 * <p>Todo ocurre en UNA transacción: si un producto no tiene stock, no se descuenta ninguno.
 */
@Service
public class InventarioService {
  private final TomosRepository tomos;
  private final FigurasRepository figuras;
  private final VentasRepository ventas;
  private final ObjectMapper json;

  public InventarioService(
      TomosRepository tomos, FigurasRepository figuras, VentasRepository ventas, ObjectMapper json) {
    this.tomos = tomos;
    this.figuras = figuras;
    this.ventas = ventas;
    this.json = json;
  }

  @Transactional
  public VentaResponse vender(String pedidoId, List<VentaItemRequest> items) {
    validar(items);
    // Orden fijo para bloquear siempre en el mismo orden (evita bloqueos cruzados).
    List<VentaItemRequest> ordenados =
        items.stream().sorted(Comparator.comparing(VentaItemRequest::clave)).toList();
    String huella = ordenados.toString();

    Optional<Venta> existente = ventas.findById(pedidoId);
    if (existente.isPresent()) {
      if (!existente.get().huella.equals(huella))
        throw new ApiException(409, "El pedido ya fue utilizado con otros productos.");
      return leer(existente.get().comprobante); // reintento: no se descuenta de nuevo
    }

    List<VentaLineaResponse> lineas = new ArrayList<>();
    int subtotal = 0;
    for (VentaItemRequest i : ordenados) {
      VentaLineaResponse linea =
          i.tipo().equals("MANGA") ? venderTomo(i) : venderFigura(i);
      lineas.add(linea);
      subtotal += linea.precio() * linea.cantidad();
    }
    VentaResponse comprobante = new VentaResponse(lineas, subtotal);
    ventas.saveAndFlush(new Venta(pedidoId, huella, escribir(comprobante)));
    return comprobante;
  }

  private VentaLineaResponse venderTomo(VentaItemRequest i) {
    Tomo t =
        tomos
            .bloquear(i.productoId(), i.tomo())
            .filter(x -> x.manga.activo)
            .orElseThrow(() -> new ApiException(409, "Un tomo del carrito ya no está disponible."));
    String nombre = t.manga.titulo + " - Tomo " + t.numero;
    if (t.stock < i.cantidad()) throw new ApiException(409, "Stock insuficiente de " + nombre + ".");
    t.stock -= i.cantidad();
    return new VentaLineaResponse("MANGA", t.manga.id, t.numero, nombre, t.precio, i.cantidad());
  }

  private VentaLineaResponse venderFigura(VentaItemRequest i) {
    Figura f =
        figuras
            .bloquear(i.productoId())
            .filter(x -> x.activo)
            .orElseThrow(
                () -> new ApiException(409, "Una figura del carrito ya no está disponible."));
    if (f.stock < i.cantidad())
      throw new ApiException(409, "Stock insuficiente de " + f.nombre + ".");
    f.stock -= i.cantidad();
    return new VentaLineaResponse("FIGURA", f.id, null, f.nombre, f.precio, i.cantidad());
  }

  private void validar(List<VentaItemRequest> items) {
    if (items == null || items.isEmpty() || items.size() > 50)
      throw new ApiException(400, "La compra debe tener entre 1 y 50 productos.");
    for (VentaItemRequest i : items) {
      boolean tipoOk = "MANGA".equals(i.tipo()) || "FIGURA".equals(i.tipo());
      if (!tipoOk || i.productoId() == null || i.cantidad() < 1)
        throw new ApiException(400, "Producto o cantidad inválidos.");
      if ("MANGA".equals(i.tipo()) && i.tomo() == null)
        throw new ApiException(400, "Falta el número de tomo.");
    }
    if (items.stream().map(VentaItemRequest::clave).distinct().count() != items.size())
      throw new ApiException(400, "Hay productos repetidos en la compra.");
  }

  private String escribir(VentaResponse c) {
    try {
      return json.writeValueAsString(c);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private VentaResponse leer(String c) {
    try {
      return json.readValue(c, VentaResponse.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
