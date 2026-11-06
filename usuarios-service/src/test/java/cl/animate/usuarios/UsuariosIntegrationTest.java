package cl.animate.usuarios;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cl.animate.usuarios.repository.UsuariosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Pruebas de integración: recorren controller → service → repository → H2 en memoria. */
@ActiveProfiles("h2")
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:usuarios-test",
      "app.keys-directory=./target/test-keys"
    })
@AutoConfigureMockMvc
class UsuariosIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired UsuariosRepository usuarios;
  @Autowired ObjectMapper json;

  private static final String LOGIN_ANA =
      "{\"correo\":\"ana@email.com\",\"contrasenia\":\"AdminAnimate#1\"}";

  private long idDe(String correo) {
    return usuarios.findByCorreo(correo).orElseThrow().id;
  }

  @Test
  void loginCorrectoEmiteJwtYCookieHttpOnly() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Animate")
                .contentType("application/json")
                .content(LOGIN_ANA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
        .andExpect(jsonPath("$.usuario.passwordHash").doesNotExist())
        .andExpect(cookie().httpOnly("animate_refresh", true));
  }

  @Test
  void loginIncorrectoDevuelve401() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Animate")
                .contentType("application/json")
                .content("{\"correo\":\"ana@email.com\",\"contrasenia\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginSinCabeceraOConOrigenAjenoDevuelve403() throws Exception {
    mvc.perform(post("/api/auth/login").contentType("application/json").content(LOGIN_ANA))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Animate")
                .header("Origin", "https://otro-sitio.example")
                .contentType("application/json")
                .content(LOGIN_ANA))
        .andExpect(status().isForbidden());
  }

  @Test
  void refreshRotaElTokenYElAntiguoDejaDeServir() throws Exception {
    var login =
        mvc.perform(
                post("/api/auth/login")
                    .header("X-Requested-With", "Animate")
                    .contentType("application/json")
                    .content(LOGIN_ANA))
            .andReturn()
            .getResponse();
    var cookie = login.getCookie("animate_refresh");
    assertNotNull(cookie);
    mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Animate").cookie(cookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty());
    // El mismo refresh token ya fue usado: debe rechazarse.
    mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Animate").cookie(cookie))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshSinCookieDevuelve401() throws Exception {
    mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Animate"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void registroCreaUsuarioAunqueIntentenEnviarRolAdmin() throws Exception {
    String correo = "nuevo" + UUID.randomUUID().toString().substring(0, 6) + "@test.cl";
    mvc.perform(
            post("/api/auth/registro")
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Felipe\",\"apellido\":\"Prueba\",\"correo\":\""
                        + correo
                        + "\",\"contrasenia\":\"ClaveSegura#12\",\"rol\":\"ADMIN\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rol").value("USUARIO"));
  }

  @Test
  void registroValidaContraseniaYNombre() throws Exception {
    mvc.perform(
            post("/api/auth/registro")
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Felipe123\",\"apellido\":\"Prueba\","
                        + "\"correo\":\"malo@test.cl\",\"contrasenia\":\"corta\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.nombre").exists())
        .andExpect(jsonPath("$.campos.contrasenia").exists());
  }

  @Test
  void registroConCorreoRepetidoDevuelve409() throws Exception {
    mvc.perform(
            post("/api/auth/registro")
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Otra\",\"apellido\":\"Ana\",\"correo\":\"ana@email.com\","
                        + "\"contrasenia\":\"ClaveSegura#12\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void contraseniaSeGuardaComoHashBCrypt() {
    assertTrue(usuarios.findByCorreo("ana@email.com").orElseThrow().passwordHash.startsWith("$2"));
  }

  @Test
  void usuariosSinTokenDevuelve401YUsuarioNormalDevuelve403() throws Exception {
    mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/usuarios").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crudUsuariosComoAdmin() throws Exception {
    String correo = "crud" + UUID.randomUUID().toString().substring(0, 6) + "@test.cl";
    String creado =
        mvc.perform(
                post("/api/usuarios")
                    .contentType("application/json")
                    .content(
                        "{\"nombre\":\"Alumno\",\"apellido\":\"Prueba\",\"correo\":\""
                            + correo
                            + "\",\"contrasenia\":\"ClaveSegura#12\",\"rol\":\"USUARIO\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = json.readTree(creado).get("id").asLong();
    mvc.perform(get("/api/usuarios/" + id)).andExpect(jsonPath("$.correo").value(correo));
    mvc.perform(
            put("/api/usuarios/" + id)
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Alumno\",\"apellido\":\"Editado\",\"correo\":\""
                        + correo
                        + "\",\"rol\":\"USUARIO\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.apellido").value("Editado"));
    mvc.perform(
            delete("/api/usuarios/" + id)
                .with(
                    jwt()
                        .jwt(j -> j.subject(String.valueOf(idDe("ana@email.com"))))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/usuarios/" + id)).andExpect(status().isNotFound());
    assertFalse(usuarios.findById(id).orElseThrow().activo);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void noSePuedeQuitarElRolAlUltimoAdmin() throws Exception {
    long id = idDe("ana@email.com");
    mvc.perform(
            put("/api/usuarios/" + id)
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Ana\",\"apellido\":\"Gonzales\","
                        + "\"correo\":\"ana@email.com\",\"rol\":\"USUARIO\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void usuarioEditaSuPerfilYGestionaDireccionesYTarjetas() throws Exception {
    long carlos = idDe("carlos@email.com");
    var token =
        jwt()
            .jwt(j -> j.subject(String.valueOf(carlos)))
            .authorities(new SimpleGrantedAuthority("ROLE_USUARIO"));

    mvc.perform(
            put("/api/usuarios/me")
                .with(token)
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Carlos\",\"apellido\":\"Calderon\","
                        + "\"correo\":\"carlos@email.com\"}"))
        .andExpect(status().isOk());

    mvc.perform(
            post("/api/direcciones")
                .with(token)
                .contentType("application/json")
                .content(
                    "{\"calle\":\"Av. Libertad 123\",\"referencia\":\"Depto 4\","
                        + "\"comuna\":\"Viña del Mar\",\"region\":\"Valparaíso\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.seleccionada").value(true))
        .andExpect(jsonPath("$.texto").value("Av. Libertad 123, Depto 4, Viña del Mar, Valparaíso"));

    mvc.perform(
            post("/api/direcciones")
                .with(token)
                .contentType("application/json")
                .content("{\"calle\":\"Sin numero\",\"comuna\":\"Viña\",\"region\":\"Valparaíso\"}"))
        .andExpect(status().isBadRequest());

    mvc.perform(
            post("/api/tarjetas")
                .with(token)
                .contentType("application/json")
                .content(
                    "{\"numero\":\"4111 1111 1111 1234\",\"clave\":\"123\","
                        + "\"propietario\":\"Carlos Calderon\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ultimos4").value("1234"))
        .andExpect(jsonPath("$.numero").doesNotExist())
        .andExpect(jsonPath("$.clave").doesNotExist());

    mvc.perform(get("/api/tarjetas").with(token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].texto").value("**** **** **** 1234 (Carlos Calderon)"));
  }
}
