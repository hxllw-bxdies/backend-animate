package cl.animate.pedidos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Producto del carrito. Solo viaja el identificador y la cantidad: el precio lo pone Catálogo, así
 * nadie puede comprar más barato modificando el JSON.
 */
public record PedidoItemRequest(
    @NotBlank @Pattern(regexp = "MANGA|FIGURA", message = "El tipo debe ser MANGA o FIGURA")
        String tipo,
    @NotNull Long productoId,
    Integer tomo,
    @Min(value = 1, message = "La cantidad mínima es 1") @Max(100) int cantidad) {}
