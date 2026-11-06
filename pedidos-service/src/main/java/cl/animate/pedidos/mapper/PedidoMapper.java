package cl.animate.pedidos.mapper;

import cl.animate.pedidos.dto.PedidoLineaResponse;
import cl.animate.pedidos.dto.PedidoResponse;
import cl.animate.pedidos.model.Pedido;

/** Convierte la entidad Pedido en el DTO que recibe React. */
public final class PedidoMapper {
  private PedidoMapper() {}

  public static PedidoResponse toResponse(Pedido p) {
    return new PedidoResponse(
        p.id,
        p.usuarioId,
        p.comprador,
        p.estado,
        p.creado,
        p.direccion,
        p.tarjeta,
        p.items.stream()
            .map(
                i ->
                    new PedidoLineaResponse(
                        i.tipo, i.productoId, i.tomo, i.nombre, i.precio, i.cantidad))
            .toList(),
        p.total,
        p.mensaje);
  }
}
