package cl.animate.catalogo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cl.animate.catalogo.dto.MangaRequest;
import cl.animate.catalogo.dto.MangaResponse;
import cl.animate.catalogo.dto.VentaItemRequest;
import cl.animate.catalogo.dto.VentaResponse;
import cl.animate.catalogo.service.InventarioService;
import cl.animate.catalogo.service.MangasService;
import cl.animate.common.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("h2")
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:catalogo-test",
      "app.internal-key=clave-de-prueba"
    })
@AutoConfigureMockMvc
class CatalogoIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired MangasService mangas;
  @Autowired InventarioService inventario;
  @Autowired ObjectMapper json;

  MangaResponse mangaDePrueba(int stock) {
    return mangas.crear(
        new MangaRequest(
            "Manga prueba " + UUID.randomUUID(),
            "SHONEN",
            "Sinopsis de prueba",
            "/Imagenes/Mangas/prueba.jpg",
            9990,
            stock));
  }

  @Test
  void catalogoEsPublicoYTraeLosDatosIniciales() throws Exception {
    mvc.perform(get("/api/mangas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].titulo").value("BOCCHI THE ROCK"))
        .andExpect(jsonPath("$[0].volumenes[0].tomo").value(1))
        .andExpect(jsonPath("$[0].tituloClave").doesNotExist());
    mvc.perform(get("/api/figuras"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].galeria").isArray());
  }

  @Test
  void crearSinSesionDevuelve401() throws Exception {
    mvc.perform(post("/api/mangas").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/mangas/papelera")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USUARIO")
  void usuarioNormalNoPuedeModificar() throws Exception {
    mvc.perform(delete("/api/mangas/1")).andExpect(status().isForbidden());
    mvc.perform(delete("/api/figuras/1")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminDebeEnviarDatosValidos() throws Exception {
    mvc.perform(
            post("/api/mangas")
                .contentType("application/json")
                .content("{\"titulo\":\"\",\"precio\":0,\"stock\":-1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.titulo").exists())
        .andExpect(jsonPath("$.campos.precio").exists());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crudMangaConTomosYPapelera() throws Exception {
    String titulo = "Chainsaw " + UUID.randomUUID().toString().substring(0, 5);
    String creado =
        mvc.perform(
                post("/api/mangas")
                    .contentType("application/json")
                    .content(
                        "{\"titulo\":\""
                            + titulo
                            + "\",\"genero\":\"SHONEN\",\"sinopsis\":\"Prueba\","
                            + "\"imgPortada\":\"/Imagenes/Mangas/cs.jpg\",\"precio\":10990,"
                            + "\"stock\":5}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.volumenes.length()").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = json.readTree(creado).get("id").asLong();

    // Título repetido (aunque cambien mayúsculas) → 409 detectado en MangasService.
    mvc.perform(
            post("/api/mangas")
                .contentType("application/json")
                .content(
                    "{\"titulo\":\""
                        + titulo.toUpperCase()
                        + "\",\"genero\":\"SHONEN\",\"sinopsis\":\"x\","
                        + "\"imgPortada\":\"/a.jpg\",\"precio\":1,\"stock\":1}"))
        .andExpect(status().isConflict());

    mvc.perform(
            post("/api/mangas/" + id + "/tomos")
                .contentType("application/json")
                .content("{\"precio\":10990,\"stock\":3,\"img\":\"/Imagenes/Mangas/cs2.jpg\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.volumenes[1].tomo").value(2));

    mvc.perform(
            put("/api/mangas/" + id + "/tomos/2")
                .contentType("application/json")
                .content("{\"precio\":12990,\"stock\":7}"))
        .andExpect(jsonPath("$.volumenes[1].precio").value(12990));

    mvc.perform(delete("/api/mangas/" + id + "/tomos/2"))
        .andExpect(jsonPath("$.volumenes.length()").value(1));

    mvc.perform(delete("/api/mangas/" + id)).andExpect(status().isNoContent());
    mvc.perform(get("/api/mangas/" + id)).andExpect(status().isNotFound());
    mvc.perform(get("/api/mangas/papelera")).andExpect(jsonPath("$[?(@.id==" + id + ")]").exists());

    mvc.perform(put("/api/mangas/" + id + "/restaurar")).andExpect(status().isOk());
    mvc.perform(get("/api/mangas/" + id)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crudFiguraYGaleria() throws Exception {
    String nombre = "Figura " + UUID.randomUUID().toString().substring(0, 5);
    String creada =
        mvc.perform(
                post("/api/figuras")
                    .contentType("application/json")
                    .content(
                        "{\"nombre\":\""
                            + nombre
                            + "\",\"proveniencia\":\"PGR\",\"tipoFigura\":\"Nendoroid\","
                            + "\"precio\":50000,\"stock\":2,\"imgPortada\":\"/Imagenes/f.webp\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = json.readTree(creada).get("id").asLong();
    mvc.perform(
            post("/api/figuras/" + id + "/galeria")
                .contentType("application/json")
                .content("{\"ruta\":\"/Imagenes/f2.webp\"}"))
        .andExpect(jsonPath("$.galeria[0]").value("/Imagenes/f2.webp"));
    mvc.perform(delete("/api/figuras/" + id + "/galeria").param("ruta", "/Imagenes/f2.webp"))
        .andExpect(jsonPath("$.galeria.length()").value(0));
    mvc.perform(
            put("/api/figuras/" + id + "/precio-stock")
                .contentType("application/json")
                .content("{\"precio\":45000,\"stock\":9}"))
        .andExpect(jsonPath("$.stock").value(9));
    mvc.perform(delete("/api/figuras/" + id)).andExpect(status().isNoContent());
    mvc.perform(put("/api/figuras/" + id + "/restaurar")).andExpect(status().isOk());
  }

  @Test
  void ventaDescuentaStockYEsIdempotente() {
    MangaResponse m = mangaDePrueba(5);
    String pedido = UUID.randomUUID().toString();
    List<VentaItemRequest> items = List.of(new VentaItemRequest("MANGA", m.id(), 1, 2));

    VentaResponse primera = inventario.vender(pedido, items);
    assertEquals(19980, primera.subtotal());
    assertEquals(3, mangas.obtener(m.id()).volumenes().get(0).stock());

    // Reintento con la misma clave: devuelve el mismo comprobante y NO descuenta otra vez.
    VentaResponse reintento = inventario.vender(pedido, items);
    assertEquals(primera, reintento);
    assertEquals(3, mangas.obtener(m.id()).volumenes().get(0).stock());
  }

  @Test
  void stockInsuficienteNoDescuentaNada() {
    MangaResponse m = mangaDePrueba(1);
    ApiException e =
        assertThrows(
            ApiException.class,
            () ->
                inventario.vender(
                    UUID.randomUUID().toString(),
                    List.of(new VentaItemRequest("MANGA", m.id(), 1, 5))));
    assertEquals(409, e.status);
    assertEquals(1, mangas.obtener(m.id()).volumenes().get(0).stock());
  }

  @Test
  void endpointInternoExigeClave() throws Exception {
    mvc.perform(
            post("/internal/ventas/" + UUID.randomUUID())
                .contentType("application/json")
                .content("[{\"tipo\":\"FIGURA\",\"productoId\":1,\"cantidad\":1}]"))
        .andExpect(status().isForbidden());
  }
}
