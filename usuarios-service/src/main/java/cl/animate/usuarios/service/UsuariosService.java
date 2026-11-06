package cl.animate.usuarios.service;

import cl.animate.common.exception.ApiException;
import cl.animate.usuarios.dto.PerfilUpdateRequest;
import cl.animate.usuarios.dto.UsuarioAdminRequest;
import cl.animate.usuarios.dto.UsuarioResponse;
import cl.animate.usuarios.dto.UsuarioUpdateRequest;
import cl.animate.usuarios.model.Usuario;
import cl.animate.usuarios.repository.RefreshRepository;
import cl.animate.usuarios.repository.UsuariosRepository;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CRUD de usuarios (administrador) y edición del propio perfil. */
@Service
public class UsuariosService {
  private final UsuariosRepository repo;
  private final RefreshRepository refresh;
  private final AuthService auth;
  private final BCryptPasswordEncoder passwords;

  public UsuariosService(
      UsuariosRepository repo,
      RefreshRepository refresh,
      AuthService auth,
      BCryptPasswordEncoder passwords) {
    this.repo = repo;
    this.refresh = refresh;
    this.auth = auth;
    this.passwords = passwords;
  }

  public List<UsuarioResponse> listar() {
    return repo.findByActivoTrueOrderByIdAsc().stream().map(AuthService::vista).toList();
  }

  public UsuarioResponse obtener(Long id) {
    return AuthService.vista(buscar(id));
  }

  @Transactional
  public UsuarioResponse crear(UsuarioAdminRequest r) {
    return AuthService.vista(
        auth.crearUsuario(r.nombre(), r.apellido(), r.correo(), r.contrasenia(), r.rol()));
  }

  @Transactional
  public UsuarioResponse editar(Long id, UsuarioUpdateRequest e) {
    Usuario u = buscar(id);
    if (u.rol.equals("ADMIN")
        && !e.rol().equals("ADMIN")
        && repo.countByRolAndActivoTrue("ADMIN") <= 1)
      throw new ApiException(409, "Debe existir al menos un administrador.");
    String correo = AuthService.normalizarCorreo(e.correo());
    if (repo.existsByCorreoAndIdNot(correo, id))
      throw new ApiException(409, "Ese correo ya está registrado");
    boolean cambioRol = !u.rol.equals(e.rol());
    u.nombre = e.nombre().trim();
    u.apellido = e.apellido().trim();
    u.correo = correo;
    u.rol = e.rol();
    // Si cambia el rol, se cierran sus sesiones para que el nuevo rol se aplique al volver a entrar.
    if (cambioRol) refresh.deleteByUsuarioId(id);
    return AuthService.vista(repo.save(u));
  }

  /** Borrado lógico: el usuario queda inactivo y se cierran sus sesiones. */
  @Transactional
  public void eliminar(Long id, Long actorId) {
    Usuario u = buscar(id);
    if (id.equals(actorId)) throw new ApiException(409, "No puedes eliminar tu propia cuenta.");
    if (u.rol.equals("ADMIN") && repo.countByRolAndActivoTrue("ADMIN") <= 1)
      throw new ApiException(409, "No puedes eliminar el último administrador.");
    u.activo = false;
    refresh.deleteByUsuarioId(id);
    repo.save(u);
  }

  /** Edición del propio perfil. Cambiar la contraseña exige la contraseña actual. */
  @Transactional
  public UsuarioResponse editarPerfil(Long id, PerfilUpdateRequest p) {
    Usuario u = buscar(id);
    String correo = AuthService.normalizarCorreo(p.correo());
    if (repo.existsByCorreoAndIdNot(correo, id))
      throw new ApiException(409, "Ese correo ya está registrado");
    if (p.contraseniaNueva() != null && !p.contraseniaNueva().isBlank()) {
      if (p.contraseniaActual() == null
          || !passwords.matches(p.contraseniaActual(), u.passwordHash))
        throw new ApiException(400, "La contraseña actual no es correcta.");
      u.passwordHash = passwords.encode(p.contraseniaNueva());
    }
    u.nombre = p.nombre().trim();
    u.apellido = p.apellido().trim();
    u.correo = correo;
    return AuthService.vista(repo.save(u));
  }

  private Usuario buscar(Long id) {
    return repo.findById(id)
        .filter(u -> u.activo)
        .orElseThrow(() -> new ApiException(404, "Usuario no encontrado."));
  }
}
