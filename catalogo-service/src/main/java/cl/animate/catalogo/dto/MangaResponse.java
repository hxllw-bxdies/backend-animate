package cl.animate.catalogo.dto;

import java.util.List;

/**
 * Misma forma que los objetos de data/mangas.js, para que React cambie lo mínimo. No expone campos
 * internos como tituloClave ni activo.
 */
public record MangaResponse(
    Long id,
    String titulo,
    String genero,
    int vistas,
    String fechaAgregado,
    String sinopsis,
    String imgPortada,
    List<TomoResponse> volumenes) {}
