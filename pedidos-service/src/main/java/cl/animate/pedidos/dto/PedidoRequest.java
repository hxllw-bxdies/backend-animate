package cl.animate.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Compra que envía la página Carrito. */
public record PedidoRequest(
    @NotEmpty(message = "El carrito está vacío") @Size(max = 50) List<@Valid PedidoItemRequest> items,
    @NotBlank(message = "Falta la dirección de despacho") @Size(min = 5, max = 400)
        String direccion,
    @NotBlank(message = "Falta la tarjeta")
        @Pattern(regexp = "\\d{4}", message = "Indica los últimos 4 dígitos de la tarjeta")
        String tarjetaUltimos4) {}
