package cl.animate.catalogo.dto;

import java.util.List;

/** Comprobante de inventario que recibe Pedidos. */
public record VentaResponse(List<VentaLineaResponse> items, int subtotal) {}
