package cl.animate.usuarios.model;

import jakarta.persistence.*;

/** Tabla usuarios. La contraseña se guarda solo como hash BCrypt, nunca en texto plano. */
@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 60)
  public String nombre;

  @Column(nullable = false, length = 60)
  public String apellido;

  @Column(nullable = false, unique = true, length = 120)
  public String correo;

  @Column(nullable = false, length = 100)
  public String passwordHash;

  /** ADMIN o USUARIO. */
  @Column(nullable = false, length = 20)
  public String rol = "USUARIO";

  /** Borrado lógico: un usuario eliminado queda inactivo y no puede iniciar sesión. */
  public boolean activo = true;

  public Usuario() {}
}
