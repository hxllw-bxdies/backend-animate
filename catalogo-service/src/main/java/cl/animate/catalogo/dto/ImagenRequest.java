package cl.animate.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Ruta de una imagen para la galería de una figura (ej: /Imagenes/Figuras/foto.webp). */
public record ImagenRequest(
    @NotBlank(message = "Indica la ruta de la imagen") @Size(max = 500) String ruta) {}
