package cl.animate.pedidos.dto;

import java.util.List;

/** Comprobante de inventario que devuelve Catálogo por la llamada interna. */
public record InventarioResponse(List<InventarioLineaResponse> items, int subtotal) {}
