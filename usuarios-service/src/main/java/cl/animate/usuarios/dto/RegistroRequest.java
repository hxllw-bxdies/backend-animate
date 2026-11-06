package cl.animate.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registro público. No trae rol: el servicio siempre crea cuentas USUARIO. */
public record RegistroRequest(
    @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60)
        @Pattern(regexp = Patrones.SOLO_LETRAS, message = "El nombre solo debe contener letras")
        String nombre,
    @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 60)
        @Pattern(regexp = Patrones.SOLO_LETRAS, message = "El apellido solo debe contener letras")
        String apellido,
    @NotBlank(message = "El correo es obligatorio")
        @Size(max = 120)
        @Email(message = "Ingrese un correo válido")
        String correo,
    @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = Patrones.CONTRASENIA, message = Patrones.MSG_CONTRASENIA)
        String contrasenia) {}
