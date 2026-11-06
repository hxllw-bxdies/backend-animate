package cl.animate.usuarios.repository;

import cl.animate.usuarios.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data genera el SQL a partir del nombre de cada método. */
@Repository
public interface UsuariosRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByCorreo(String correo);

  boolean existsByCorreo(String correo);

  boolean existsByCorreoAndIdNot(String correo, Long id);

  List<Usuario> findByActivoTrueOrderByIdAsc();

  long countByRolAndActivoTrue(String rol);
}
