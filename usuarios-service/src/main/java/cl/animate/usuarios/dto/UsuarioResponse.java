package cl.animate.usuarios.dto;

/** Lo que ve React de un usuario. No incluye passwordHash. */
public record UsuarioResponse(Long id, String nombre, String apellido, String correo, String rol) {}
