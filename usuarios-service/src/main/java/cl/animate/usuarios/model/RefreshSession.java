package cl.animate.usuarios.model;

import jakarta.persistence.*;
import java.time.Instant;

/** Tabla sesiones_refresh: guarda el HASH del refresh token (no el token), su dueño y vencimiento. */
@Entity
@Table(name = "sesiones_refresh")
public class RefreshSession {
  @Id
  @Column(length = 64)
  public String tokenHash;

  @Column(nullable = false)
  public Long usuarioId;

  @Column(nullable = false)
  public Instant expira;

  public RefreshSession() {}

  public RefreshSession(String hash, Long usuario, Instant vencimiento) {
    tokenHash = hash;
    usuarioId = usuario;
    expira = vencimiento;
  }
}
