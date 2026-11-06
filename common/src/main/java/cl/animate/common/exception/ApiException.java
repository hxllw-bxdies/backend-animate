package cl.animate.common.exception;

/** Error de negocio conocido: lleva el estado HTTP que se devolverá al frontend. */
public class ApiException extends RuntimeException {
  public final int status;

  public ApiException(int status, String message) {
    super(message);
    this.status = status;
  }
}
