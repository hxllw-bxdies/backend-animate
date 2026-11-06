package cl.animate.usuarios.dto;

/** texto: "**** **** **** 1234 (Propietario)", igual que textoTarjeta() del frontend. */
public record TarjetaResponse(
    Long id, String ultimos4, String propietario, boolean seleccionada, String texto) {}
