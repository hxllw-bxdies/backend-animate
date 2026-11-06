package cl.animate.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Datos que envía el formulario de inicio de sesión. */
public record LoginRequest(
    @NotBlank(message = "El correo es obligatorio") @Email(message = "Ingrese un correo válido")
        String correo,
    @NotBlank(message = "La contraseña es obligatoria") String contrasenia) {}
