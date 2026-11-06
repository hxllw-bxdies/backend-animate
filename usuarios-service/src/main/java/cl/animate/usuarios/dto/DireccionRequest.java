package cl.animate.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Mismas reglas que validarDireccion() de src/utils/direcciones.js. */
public record DireccionRequest(
    @NotBlank(message = "La calle es obligatoria")
        @Size(max = 150)
        @Pattern(regexp = ".*\\d.*", message = "Incluya el número de la dirección")
        String calle,
    @Size(max = 150) String referencia,
    @NotBlank(message = "La comuna es obligatoria")
        @Size(max = 80)
        @Pattern(regexp = Patrones.SOLO_LETRAS, message = "La comuna solo debe contener letras")
        String comuna,
    @NotBlank(message = "Seleccione una región") String region) {}
