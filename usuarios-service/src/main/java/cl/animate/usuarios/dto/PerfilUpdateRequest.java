package cl.animate.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Edición del propio perfil (PerfilUsuario / PerfilAdmin). La contraseña es opcional: si viene
 * contraseniaNueva, también debe venir contraseniaActual.
 */
public record PerfilUpdateRequest(
    @NotBlank @Size(max = 60) @Pattern(regexp = Patrones.SOLO_LETRAS, message = "Solo letras")
        String nombre,
    @NotBlank @Size(max = 60) @Pattern(regexp = Patrones.SOLO_LETRAS, message = "Solo letras")
        String apellido,
    @NotBlank @Size(max = 120) @Email(message = "Ingrese un correo válido") String correo,
    String contraseniaActual,
    @Pattern(regexp = Patrones.CONTRASENIA, message = Patrones.MSG_CONTRASENIA)
        String contraseniaNueva) {}
