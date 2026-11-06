package cl.animate.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Edición de los datos generales de un manga (los tomos se editan aparte). */
public record MangaUpdateRequest(
    @NotBlank(message = "El título es obligatorio") @Size(max = 150) String titulo,
    @NotBlank(message = "El género es obligatorio") @Size(max = 40) String genero,
    @NotBlank(message = "La sinopsis es obligatoria") @Size(max = 2000) String sinopsis,
    @NotBlank(message = "La imagen es obligatoria") @Size(max = 500) String imgPortada) {}
