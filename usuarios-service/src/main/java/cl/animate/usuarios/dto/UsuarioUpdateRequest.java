package cl.animate.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Edición de un usuario por parte del administrador. */
public record UsuarioUpdateRequest(
    @NotBlank @Size(max = 60) @Pattern(regexp = Patrones.SOLO_LETRAS, message = "Solo letras")
        String nombre,
    @NotBlank @Size(max = 60) @Pattern(regexp = Patrones.SOLO_LETRAS, message = "Solo letras")
        String apellido,
    @NotBlank @Size(max = 120) @Email(message = "Ingrese un correo válido") String correo,
    @NotBlank @Pattern(regexp = "ADMIN|USUARIO", message = "El rol debe ser ADMIN o USUARIO")
        String rol) {}
