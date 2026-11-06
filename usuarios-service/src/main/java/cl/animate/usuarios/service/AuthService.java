package cl.animate.usuarios.service;

import cl.animate.common.exception.ApiException;
import cl.animate.common.security.SecuritySupport;
import cl.animate.usuarios.dto.AuthResultado;
import cl.animate.usuarios.dto.RegistroRequest;
import cl.animate.usuarios.dto.SesionResponse;
import cl.animate.usuarios.dto.UsuarioResponse;
import cl.animate.usuarios.model.RefreshSession;
import cl.animate.usuarios.model.Usuario;
import cl.animate.usuarios.repository.RefreshRepository;
import cl.animate.usuarios.repository.UsuariosRepository;
import cl.animate.usuarios.security.JwtKeys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticación: login, registro, emisión de JWT y renovación con refresh token.
 *
 * <p>Access token (JWT RS256): dura pocos minutos y lleva el id del usuario (sub) y su rol (roles).
 * Refresh token: valor aleatorio que va en una cookie HttpOnly; en la base solo se guarda su hash
 * SHA-256. Cada renovación elimina el refresh usado y entrega uno nuevo (rotación).
 */
@Service
public class AuthService {
  public static final Duration DURACION_REFRESH = Duration.ofDays(7);

  private final UsuariosRepository usuarios;
  private final RefreshRepository sesiones;
  private final BCryptPasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final String issuer;
  private final long segundos;
  private final SecureRandom random = new SecureRandom();

  public AuthService(
      UsuariosRepository usuarios,
      RefreshRepository sesiones,
      BCryptPasswordEncoder passwords,
      JwtKeys keys,
      @Value("${app.issuer}") String issuer,
      @Value("${app.access-seconds}") long segundos) {
    this.usuarios = usuarios;
    this.sesiones = sesiones;
    this.passwords = passwords;
    this.encoder = keys.encoder();
    this.issuer = issuer;
    this.segundos = segundos;
  }

  /** Convierte la entidad en DTO de salida (sin el hash de la contraseña). */
  public static UsuarioResponse vista(Usuario u) {
    return new UsuarioResponse(u.id, u.nombre, u.apellido, u.correo, u.rol);
  }

  public static String normalizarCorreo(String correo) {
    return correo.trim().toLowerCase(Locale.ROOT);
  }

  public Usuario usuarioActivo(Long id) {
    return usuarios
        .findById(id)
        .filter(u -> u.activo)
        .orElseThrow(() -> new ApiException(401, "La cuenta no está activa."));
  }

  @Transactional
  public AuthResultado login(String correo, String contrasenia) {
    Usuario u =
        usuarios
            .findByCorreo(normalizarCorreo(correo))
            .filter(x -> x.activo && passwords.matches(contrasenia, x.passwordHash))
            .orElseThrow(() -> new ApiException(401, "Correo o contraseña incorrectos."));
    return emitir(u);
  }

  @Transactional
  public AuthResultado refresh(String token) {
    RefreshSession s =
        sesiones
            .bloquear(hash(token))
            .filter(x -> x.expira.isAfter(Instant.now()))
            .orElseThrow(
                () -> new ApiException(401, "La sesión expiró. Inicia sesión nuevamente."));
    Usuario u = usuarioActivo(s.usuarioId);
    sesiones.delete(s);
    sesiones.flush();
    return emitir(u);
  }

  @Transactional
  public void logout(String token) {
    if (token != null && !token.isBlank()) sesiones.deleteById(hash(token));
  }

  /** Registro público: el rol siempre es USUARIO aunque el JSON intente enviar otro. */
  @Transactional
  public UsuarioResponse registrar(RegistroRequest r) {
    return vista(crearUsuario(r.nombre(), r.apellido(), r.correo(), r.contrasenia(), "USUARIO"));
  }

  @Transactional
  public Usuario crearUsuario(
      String nombre, String apellido, String correo, String contrasenia, String rol) {
    String mail = normalizarCorreo(correo);
    if (usuarios.existsByCorreo(mail)) throw new ApiException(409, "Ese correo ya está registrado");
    Usuario u = new Usuario();
    u.nombre = nombre.trim();
    u.apellido = apellido.trim();
    u.correo = mail;
    u.passwordHash = passwords.encode(contrasenia);
    u.rol = rol;
    return usuarios.save(u);
  }

  private AuthResultado emitir(Usuario u) {
    Instant ahora = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(u.id.toString())
            .audience(List.of(SecuritySupport.AUDIENCIA))
            .issuedAt(ahora)
            .expiresAt(ahora.plusSeconds(segundos))
            .claim("roles", List.of(u.rol))
            .claim("correo", u.correo)
            .claim("nombre", u.nombre + " " + u.apellido)
            .build();
    String access =
        encoder
            .encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
            .getTokenValue();
    byte[] bytes = new byte[48];
    random.nextBytes(bytes);
    String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    sesiones.save(new RefreshSession(hash(refresh), u.id, ahora.plus(DURACION_REFRESH)));
    return new AuthResultado(new SesionResponse(access, segundos, vista(u)), refresh);
  }

  static String hash(String valor) {
    if (valor == null || valor.isBlank())
      throw new ApiException(401, "No existe una sesión activa.");
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
