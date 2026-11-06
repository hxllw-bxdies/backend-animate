package cl.animate.usuarios.repository;

import cl.animate.usuarios.model.RefreshSession;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshSession, String> {
  /** Bloquea la fila para que el mismo refresh token no se pueda usar dos veces a la vez. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from RefreshSession s where s.tokenHash = :hash")
  Optional<RefreshSession> bloquear(@Param("hash") String hash);

  void deleteByUsuarioId(Long usuarioId);
}
