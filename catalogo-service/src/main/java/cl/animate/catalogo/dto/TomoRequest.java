package cl.animate.catalogo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nuevo tomo. El número se asigna solo (el siguiente al mayor), como generarNuevoTomo(). */
public record TomoRequest(
    @NotNull(message = "El precio es obligatorio") @Min(value = 1, message = "El precio debe ser mayor a 0") @Max(10000000)
        Integer precio,
    @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") @Max(100000)
        Integer stock,
    @NotBlank(message = "Indica la ruta de la imagen del tomo.") @Size(max = 500) String img) {}
