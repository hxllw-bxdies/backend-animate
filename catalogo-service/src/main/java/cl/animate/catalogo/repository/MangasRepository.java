package cl.animate.catalogo.repository;

import cl.animate.catalogo.model.Manga;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MangasRepository extends JpaRepository<Manga, Long> {
  /** Catálogo visible. */
  List<Manga> findByActivoTrueOrderByIdAsc();

  /** Papelera. */
  List<Manga> findByActivoFalseOrderByIdAsc();

  boolean existsByTituloClaveAndIdNot(String tituloClave, Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Manga m where m.id = :id")
  Optional<Manga> bloquear(@Param("id") Long id);
}
