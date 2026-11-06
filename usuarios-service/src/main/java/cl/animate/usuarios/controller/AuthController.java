package cl.animate.usuarios.controller;

import cl.animate.common.exception.ApiException;
import cl.animate.usuarios.dto.AuthResultado;
import cl.animate.usuarios.dto.LoginRequest;
import cl.animate.usuarios.dto.RegistroRequest;
import cl.animate.usuarios.dto.SesionResponse;
import cl.animate.usuarios.dto.UsuarioResponse;
import cl.animate.usuarios.security.JwtKeys;
import cl.animate.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Login, registro, renovación y cierre de sesión")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  public static final String COOKIE = "animate_refresh";
  public static final String CABECERA_VALOR = "Animate";

  private final AuthService service;
  private final JwtKeys keys;
  private final boolean secure;
  private final Set<String> origenes;

  public AuthController(
      AuthService service,
      JwtKeys keys,
      @Value("${app.cookie-secure}") boolean secure,
      @Value("${app.allowed-origins}") String origins,
      @Value("${app.gateway-url}") String gatewayUrl) {
    this.service = service;
    this.keys = keys;
    this.secure = secure;
    Set<String> permitidos = new HashSet<>(Arrays.asList(origins.split(",")));
    permitidos.add(gatewayUrl); // permite probar el login desde Swagger del Gateway
    this.origenes = Set.copyOf(permitidos);
  }

  @Operation(summary = "Inicia sesión con correo y contraseña. Devuelve el JWT y deja la cookie.")
  @Parameter(
      name = "X-Requested-With",
      in = ParameterIn.HEADER,
      required = true,
      schema = @Schema(defaultValue = CABECERA_VALOR))
  @PostMapping("/login")
  public SesionResponse login(
      @Valid @RequestBody LoginRequest form, HttpServletRequest req, HttpServletResponse res) {
    validarNavegador(req);
    AuthResultado r = service.login(form.correo(), form.contrasenia());
    cookie(res, r.refresh(), AuthService.DURACION_REFRESH.toSeconds());
    return r.sesion();
  }

  @Operation(summary = "Registro público. Siempre crea cuentas con rol USUARIO.")
  @PostMapping("/registro")
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse registro(@Valid @RequestBody RegistroRequest r) {
    return service.registrar(r);
  }

  @Operation(summary = "Renueva el JWT usando la cookie HttpOnly (se usa al recargar la página).")
  @Parameter(
      name = "X-Requested-With",
      in = ParameterIn.HEADER,
      required = true,
      schema = @Schema(defaultValue = CABECERA_VALOR))
  @PostMapping("/refresh")
  public SesionResponse refresh(
      @CookieValue(value = COOKIE, required = false) String token,
      HttpServletRequest req,
      HttpServletResponse res) {
    validarNavegador(req);
    AuthResultado r = service.refresh(token);
    cookie(res, r.refresh(), AuthService.DURACION_REFRESH.toSeconds());
    return r.sesion();
  }

  @Operation(summary = "Cierra la sesión: borra el refresh token de la base y la cookie.")
  @Parameter(
      name = "X-Requested-With",
      in = ParameterIn.HEADER,
      required = true,
      schema = @Schema(defaultValue = CABECERA_VALOR))
  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      @CookieValue(value = COOKIE, required = false) String token,
      HttpServletRequest req,
      HttpServletResponse res) {
    validarNavegador(req);
    service.logout(token);
    cookie(res, "", 0);
  }

  @Operation(summary = "Datos del usuario dueño del token.")
  @GetMapping("/me")
  public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
    return AuthService.vista(service.usuarioActivo(Long.valueOf(jwt.getSubject())));
  }

  @Operation(summary = "Clave pública (JWKS) con la que los otros servicios validan los JWT.")
  @GetMapping("/jwks")
  public Map<String, Object> jwks() {
    return keys.publicJwks();
  }

  /**
   * Protección extra para las rutas que usan la cookie: exige la cabecera X-Requested-With y un
   * origen permitido. Otro sitio web no puede enviar esa cabecera sin pasar por CORS.
   */
  private void validarNavegador(HttpServletRequest req) {
    String origen = req.getHeader("Origin");
    if (!CABECERA_VALOR.equals(req.getHeader("X-Requested-With"))
        || (origen != null && !origenes.contains(origen)))
      throw new ApiException(403, "Origen o encabezado de sesión no permitido.");
  }

  /** Cookie HttpOnly (JavaScript no la puede leer), SameSite=Strict y limitada a /api/auth. */
  private void cookie(HttpServletResponse res, String valor, long segundos) {
    res.addHeader(
        HttpHeaders.SET_COOKIE,
        ResponseCookie.from(COOKIE, valor)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path("/api/auth")
            .maxAge(segundos)
            .build()
            .toString());
  }
}
