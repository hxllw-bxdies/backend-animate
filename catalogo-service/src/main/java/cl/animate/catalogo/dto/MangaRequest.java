package cl.animate.catalogo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Alta de un manga desde el panel admin: se crea con su tomo 1 (igual que agregarManga()). */
public record MangaRequest(
    @NotBlank(message = "El título es obligatorio") @Size(max = 150) String titulo,
    @NotBlank(message = "El género es obligatorio") @Size(max = 40) String genero,
    @NotBlank(message = "La sinopsis es obligatoria") @Size(max = 2000) String sinopsis,
    @NotBlank(message = "La imagen es obligatoria") @Size(max = 500) String imgPortada,
    @NotNull(message = "El precio es obligatorio") @Min(value = 1, message = "El precio debe ser mayor a 0") @Max(10000000)
        Integer precio,
    @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") @Max(100000)
        Integer stock) {}
