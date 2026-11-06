package cl.animate.usuarios.dto;

/** Respuesta de login/refresh: access token (JWT), segundos de vigencia y datos del usuario. */
public record SesionResponse(String accessToken, long expiresIn, UsuarioResponse usuario) {}
