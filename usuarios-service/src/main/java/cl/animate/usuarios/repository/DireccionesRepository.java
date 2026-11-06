package cl.animate.usuarios.repository;

import cl.animate.usuarios.model.Direccion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireccionesRepository extends JpaRepository<Direccion, Long> {
  List<Direccion> findByUsuarioIdOrderByIdAsc(Long usuarioId);

  Optional<Direccion> findByIdAndUsuarioId(Long id, Long usuarioId);
}
