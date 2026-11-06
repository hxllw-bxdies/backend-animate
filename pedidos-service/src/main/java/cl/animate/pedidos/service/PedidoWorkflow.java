package cl.animate.pedidos.service;

import cl.animate.common.exception.ApiException;
import cl.animate.pedidos.dto.InventarioResponse;
import cl.animate.pedidos.dto.PedidoRequest;
import cl.animate.pedidos.model.Pedido;
import java.time.Duration;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Flujo de una compra entre DOS microservicios con bases separadas:
 *
 * <ol>
 *   <li>Pedidos guarda el pedido como PENDIENTE en su base.
 *   <li>Llama por HTTP interno a Catálogo (/internal/ventas/{id}), que descuenta el stock y
 *       devuelve un comprobante con los precios vigentes.
 *   <li>Pedidos guarda las líneas y marca el pedido CONFIRMADO.
 * </ol>
 *
 * Si se pierde la respuesta, el pedido queda PENDIENTE; al reintentar con la misma
 * Idempotency-Key, Catálogo devuelve el mismo comprobante sin descontar otra vez.
 */
@Service
public class PedidoWorkflow {
  private final PedidoStore store;
  private final RestClient client;
  private final String clave;

  public PedidoWorkflow(
      PedidoStore store,
      @Value("${app.catalogo-url}") String catalogoUrl,
      @Value("${app.internal-key}") String clave) {
    this.store = store;
    this.clave = clave;
    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(Duration.ofSeconds(3));
    f.setReadTimeout(Duration.ofSeconds(5));
    this.client = RestClient.builder().baseUrl(catalogoUrl).requestFactory(f).build();
  }

  public synchronized Pedido comprar(String id, Long usuarioId, String correo, PedidoRequest form) {
    Pedido p = store.iniciar(id, usuarioId, correo, form);
    if (!p.estado.equals("PENDIENTE")) return p;
    try {
      InventarioResponse comprobante =
          client
              .post()
              .uri("/internal/ventas/{id}", id)
              .header("X-Internal-Key", clave)
              .body(form.items())
              .retrieve()
              .body(InventarioResponse.class);
      return store.confirmar(id, Objects.requireNonNull(comprobante));
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode().value() == 403)
        throw new ApiException(
            503, "No se pudo comunicar con inventario. Reintenta con la misma clave.");
      // 400/409 de Catálogo: producto inexistente o sin stock. El pedido queda RECHAZADO.
      String motivo = "No fue posible confirmar los productos o el stock. Revisa tu carrito.";
      store.rechazar(id, motivo);
      throw new ApiException(409, motivo);
    } catch (RestClientException ex) {
      throw new ApiException(
          503, "Inventario no responde. El pedido sigue pendiente; reintenta sin cambiar la clave.");
    }
  }
}
