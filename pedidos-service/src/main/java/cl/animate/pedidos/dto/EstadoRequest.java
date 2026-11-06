package cl.animate.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Cambio de estado por el administrador. */
public record EstadoRequest(
    @NotBlank @Pattern(regexp = "ENVIADO|ENTREGADO", message = "Estado inválido") String estado) {}
