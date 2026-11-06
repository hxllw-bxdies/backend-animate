package cl.animate.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Creación de usuarios desde el panel de administración (sí puede elegir el rol). */
public record UsuarioAdminRequest(
    @NotBlank @Size(max = 60) @Pattern(regexp = Patrones.SOLO_LETRAS, message = "Solo letras")
        String nombre,
    @NotBlank @Size(max = 60) @Pattern(regexp = Patrones.SOLO_LETRAS, message = "Solo letras")
        String apellido,
    @NotBlank @Size(max = 120) @Email(message = "Ingrese un correo válido") String correo,
    @NotBlank @Pattern(regexp = Patrones.CONTRASENIA, message = Patrones.MSG_CONTRASENIA)
        String contrasenia,
    @NotBlank @Pattern(regexp = "ADMIN|USUARIO", message = "El rol debe ser ADMIN o USUARIO")
        String rol) {}
