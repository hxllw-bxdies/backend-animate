package cl.animate.catalogo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Alta o edición de una figura. */
public record FiguraRequest(
    @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String nombre,
    @NotBlank(message = "La proveniencia es obligatoria") @Size(max = 100) String proveniencia,
    @NotBlank(message = "El tipo de figura es obligatorio") @Size(max = 60) String tipoFigura,
    @NotNull(message = "El precio es obligatorio") @Min(value = 1, message = "El precio debe ser mayor a 0") @Max(10000000)
        Integer precio,
    @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") @Max(100000)
        Integer stock,
    @NotBlank(message = "La imagen es obligatoria") @Size(max = 500) String imgPortada) {}
