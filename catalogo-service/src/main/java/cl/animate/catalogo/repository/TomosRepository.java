package cl.animate.catalogo.repository;

import cl.animate.catalogo.model.Tomo;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TomosRepository extends JpaRepository<Tomo, Long> {
  /**
   * Bloquea la fila del tomo mientras se descuenta stock: si dos personas compran la última unidad
   * al mismo tiempo, la segunda espera y luego ve stock 0.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from Tomo t where t.manga.id = :mangaId and t.numero = :numero")
  Optional<Tomo> bloquear(@Param("mangaId") Long mangaId, @Param("numero") int numero);
}
