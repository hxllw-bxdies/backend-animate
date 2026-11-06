package cl.animate.catalogo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Cambio de precio y stock de un tomo o de una figura. */
public record PrecioStockRequest(
    @NotNull(message = "El precio es obligatorio") @Min(value = 1, message = "El precio debe ser mayor a 0") @Max(10000000)
        Integer precio,
    @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") @Max(100000)
        Integer stock) {}
