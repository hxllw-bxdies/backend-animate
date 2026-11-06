package cl.animate.pedidos;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cl.animate.common.exception.ApiException;
import cl.animate.pedidos.dto.InventarioLineaResponse;
import cl.animate.pedidos.dto.InventarioResponse;
import cl.animate.pedidos.dto.PedidoItemRequest;
import cl.animate.pedidos.dto.PedidoRequest;
import cl.animate.pedidos.model.Pedido;
import cl.animate.pedidos.repository.PedidosRepository;
import cl.animate.pedidos.service.PedidoStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("h2")
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:pedidos-test",
      // Catálogo apagado a propósito para probar el caso "inventario no responde".
      "app.catalogo-url=http://localhost:9"
    })
@AutoConfigureMockMvc
class PedidosIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired PedidoStore store;
  @Autowired PedidosRepository repo;

  Jwt token(String id, String rol) {
    return Jwt.withTokenValue("test")
        .header("alg", "RS256")
        .subject(id)
        .claim("roles", List.of(rol))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(60))
        .build();
  }

  PedidoRequest form() {
    return new PedidoRequest(
        List.of(new PedidoItemRequest("MANGA", 1L, 2, 1), new PedidoItemRequest("FIGURA", 3L, null, 1)),
        "Av. Libertad 123, Viña del Mar, Valparaíso",
        "1234");
  }

  @Test
  void pedidoSinSesionDevuelve401() throws Exception {
    mvc.perform(get("/api/pedidos/mios")).andExpect(status().isUnauthorized());
  }

  @Test
  void usuarioNoPuedeVerTodasLasVentas() throws Exception {
    mvc.perform(
            get("/api/pedidos").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USUARIO"))))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/pedidos").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }

  @Test
  void compraSinIdempotencyKeyOConCarritoVacioDevuelve400() throws Exception {
    var usuario = jwt().jwt(j -> j.subject("2")).authorities(new SimpleGrantedAuthority("ROLE_USUARIO"));
    mvc.perform(
            post("/api/pedidos")
                .with(usuario)
                .contentType("application/json")
                .content("{\"items\":[],\"direccion\":\"Calle 123\",\"tarjetaUltimos4\":\"1234\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/pedidos")
                .with(usuario)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType("application/json")
                .content("{\"items\":[],\"direccion\":\"Calle 123\",\"tarjetaUltimos4\":\"1234\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.items").exists());
  }

  @Test
  void siCatalogoNoRespondeElPedidoQuedaPendiente() throws Exception {
    String clave = UUID.randomUUID().toString();
    mvc.perform(
            post("/api/pedidos")
                .with(jwt().jwt(j -> j.subject("2")).authorities(new SimpleGrantedAuthority("ROLE_USUARIO")))
                .header("Idempotency-Key", clave)
                .contentType("application/json")
                .content(
                    "{\"items\":[{\"tipo\":\"MANGA\",\"productoId\":1,\"tomo\":1,\"cantidad\":1}],"
                        + "\"direccion\":\"Calle 123, Viña\",\"tarjetaUltimos4\":\"1234\"}"))
        .andExpect(status().isServiceUnavailable());
    assertEquals("PENDIENTE", repo.findById(clave).orElseThrow().estado);
  }

  @Test
  void mismaClaveConOtrosDatosDevuelveConflicto() {
    String id = UUID.randomUUID().toString();
    store.iniciar(id, 2L, "carlos@email.com", form());
    ApiException e =
        assertThrows(
            ApiException.class,
            () ->
                store.iniciar(
                    id,
                    2L,
                    "carlos@email.com",
                    new PedidoRequest(form().items(), "Otra dirección 456", "1234")));
    assertEquals(409, e.status);
  }

  @Test
  void usuarioNoPuedeLeerPedidoAjeno() {
    String id = UUID.randomUUID().toString();
    store.iniciar(id, 2L, "carlos@email.com", form());
    assertThrows(ApiException.class, () -> store.obtener(id, token("3", "USUARIO")));
    assertEquals(id, store.obtener(id, token("1", "ADMIN")).id);
  }

  @Test
  void confirmacionGuardaPreciosDeLaCompraYPermiteCambiarEstado() {
    String id = UUID.randomUUID().toString();
    store.iniciar(id, 2L, "carlos@email.com", form());
    Pedido p =
        store.confirmar(
            id,
            new InventarioResponse(
                List.of(
                    new InventarioLineaResponse("MANGA", 1L, 2, "BOCCHI THE ROCK - Tomo 2", 14990, 1),
                    new InventarioLineaResponse("FIGURA", 3L, null, "Lucia Crimson Wave", 175000, 1)),
                189990));
    assertEquals("CONFIRMADO", p.estado);
    assertEquals(189990, p.total);
    assertEquals("**** 1234", p.tarjeta);
    assertEquals(2, p.items.size());
    assertEquals("ENVIADO", store.cambiarEstado(id, "ENVIADO").estado);
    assertThrows(ApiException.class, () -> store.cambiarEstado(id, "ENVIADO"));
    assertEquals(1, store.mios(2L).stream().filter(x -> x.id.equals(id)).count());
  }
}
