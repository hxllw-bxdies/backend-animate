package cl.animate.usuarios.dto;

/**
 * Expresiones regulares iguales a las del frontend (src/utils/usuarios.js), para que el backend
 * valide lo mismo aunque alguien llame a la API sin pasar por React.
 */
public final class Patrones {
  /** Solo letras y espacios, con tildes y ñ. */
  public static final String SOLO_LETRAS = "^[A-Za-zÁÉÍÓÚÑáéíóúñ\\s]+$";

  /** Mínimo 12 caracteres, al menos una mayúscula y un carácter especial (# $ % & *). */
  public static final String CONTRASENIA = "^(?=.*[A-ZÁÉÍÓÚÑ])(?=.*[#$%&*]).{12,72}$";

  public static final String MSG_CONTRASENIA =
      "La contraseña debe tener al menos 12 caracteres, una mayúscula y un carácter especial"
          + " (# $ % & *)";

  private Patrones() {}
}
