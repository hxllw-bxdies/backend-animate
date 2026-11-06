package cl.animate.usuarios.dto;

/** Uso interno: la sesión va en el JSON y el refresh token va solo en la cookie HttpOnly. */
public record AuthResultado(SesionResponse sesion, String refresh) {}
