package cl.animate.usuarios.repository;

import cl.animate.usuarios.model.Tarjeta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarjetasRepository extends JpaRepository<Tarjeta, Long> {
  List<Tarjeta> findByUsuarioIdOrderByIdAsc(Long usuarioId);

  Optional<Tarjeta> findByIdAndUsuarioId(Long id, Long usuarioId);
}
