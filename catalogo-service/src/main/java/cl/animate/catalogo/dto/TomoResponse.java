package cl.animate.catalogo.dto;

/** Mismos nombres que usa el frontend en data/mangas.js: { tomo, precio, stock, img }. */
public record TomoResponse(int tomo, int precio, int stock, String img) {}
