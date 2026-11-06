package cl.animate.usuarios.config;

import cl.animate.usuarios.model.Usuario;
import cl.animate.usuarios.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Cuentas iniciales (las mismas de src/utils/usuarios.js). Solo se crean si no existen. */
@Configuration
public class Seed {
  @Bean
  CommandLineRunner cargarUsuarios(
      UsuariosRepository repo,
      BCryptPasswordEncoder encoder,
      @Value("${app.admin-password}") String adminPass,
      @Value("${app.usuario-password}") String usuarioPass) {
    return args -> {
      crear(repo, encoder, "Ana", "Gonzales", "ana@email.com", adminPass, "ADMIN");
      crear(repo, encoder, "Carlos", "Calderon", "carlos@email.com", usuarioPass, "USUARIO");
    };
  }

  private void crear(
      UsuariosRepository repo,
      BCryptPasswordEncoder encoder,
      String nombre,
      String apellido,
      String correo,
      String pass,
      String rol) {
    if (repo.existsByCorreo(correo)) return;
    Usuario u = new Usuario();
    u.nombre = nombre;
    u.apellido = apellido;
    u.correo = correo;
    u.rol = rol;
    u.passwordHash = encoder.encode(pass);
    repo.save(u);
  }
}
