package cl.animate.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * El número y la clave solo se validan; el servicio guarda únicamente los últimos 4 dígitos.
 * Mismas reglas que validarTarjeta() de src/utils/tarjetas.js.
 */
public record TarjetaRequest(
    @NotBlank @Pattern(regexp = "^(\\d\\s?){16}$", message = "El número debe tener 16 dígitos")
        String numero,
    @NotBlank @Pattern(regexp = "^\\d{3}$", message = "La clave debe tener 3 dígitos")
        String clave,
    @NotBlank(message = "El propietario es obligatorio")
        @Size(max = 100)
        @Pattern(
            regexp = Patrones.SOLO_LETRAS,
            message = "El propietario solo debe contener letras")
        String propietario) {}
