package cl.animate.catalogo.dto;

import java.util.List;

/** Misma forma que los objetos de data/figuras.js. */
public record FiguraResponse(
    Long id,
    String nombre,
    String proveniencia,
    String tipoFigura,
    int stock,
    int precio,
    int vistas,
    String fechaAgregado,
    String imgPortada,
    List<String> galeria) {}
