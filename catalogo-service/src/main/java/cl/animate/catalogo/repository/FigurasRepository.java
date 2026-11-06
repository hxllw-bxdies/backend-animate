package cl.animate.catalogo.repository;

import cl.animate.catalogo.model.Figura;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FigurasRepository extends JpaRepository<Figura, Long> {
  List<Figura> findByActivoTrueOrderByIdAsc();

  List<Figura> findByActivoFalseOrderByIdAsc();

  boolean existsByNombreClaveAndIdNot(String nombreClave, Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select f from Figura f where f.id = :id")
  Optional<Figura> bloquear(@Param("id") Long id);
}
