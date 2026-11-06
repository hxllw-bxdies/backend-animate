package cl.animate.usuarios.dto;

/** texto: dirección lista para mostrar, igual que textoDireccion() del frontend. */
public record DireccionResponse(
    Long id,
    String calle,
    String referencia,
    String comuna,
    String region,
    boolean seleccionada,
    String texto) {}
