package cl.animate.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** scanBasePackages incluye cl.animate.common para cargar Swagger y errores compartidos. */
@SpringBootApplication(scanBasePackages = "cl.animate")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
